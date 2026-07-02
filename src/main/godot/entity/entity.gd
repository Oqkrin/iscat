extends RigidBody2D
class_name Entity

@export var dynamics_data: DynamicsData
@export var visual_data: VisualData

@onready var sprite: AnimatedSprite2D = $AnimatedSprite2D

var visual_controller: VisualController
var rotation_controller: RotationController

# Generalized inputs that can be set by a Player Controller or an AI/Steering Behavior
var move_direction: Vector2 = Vector2.ZERO
var target_rotation_pos: Vector2 = Vector2.ZERO

func _ready() -> void:
	# Fallback initialization
	if not dynamics_data: dynamics_data = DynamicsData.new()
	if not visual_data: visual_data = VisualData.new()
	
	# Apply physics properties
	mass = dynamics_data.mass
	# We handle our own friction/damping linearly in _integrate_forces
	linear_damp = 0.0
	angular_damp = 0.0
	gravity_scale = 0.0 # Top-down, no gravity
	lock_rotation = true # We handle visual rotation manually
	
	# PREVENT SLEEPING: otherwise player stops moving and physics engine ignores it
	can_sleep = false 
	
	visual_controller = VisualController.new(sprite, visual_data)
	rotation_controller = RotationController.new(self, dynamics_data.angular_velocity)

func _physics_process(delta: float) -> void:
	# 1. Update visual rotation based on the generalized target position
	if target_rotation_pos != Vector2.ZERO:
		global_rotation = rotation_controller.get_rad_towards_pos(target_rotation_pos, delta, visual_data.sprite_offset_rad)
	
	# 2. Update visual animation
	visual_controller.play_movement_animation(move_direction != Vector2.ZERO)

func _integrate_forces(state: PhysicsDirectBodyState2D) -> void:
	var current_velocity = state.linear_velocity
	var dt = state.step
	
	if move_direction != Vector2.ZERO:
		# Apply acceleration force
		var accel_force = move_direction.normalized() * dynamics_data.acceleration
		state.apply_central_force(accel_force)
		
		# Clamp velocity to terminal_velocity
		if state.linear_velocity.length() > dynamics_data.terminal_velocity:
			state.linear_velocity = state.linear_velocity.normalized() * dynamics_data.terminal_velocity
	else:
		# Apply friction to stop
		if current_velocity.length() > 0:
			var friction_drop = dynamics_data.friction * dt
			if current_velocity.length() < friction_drop:
				state.linear_velocity = Vector2.ZERO
			else:
				state.linear_velocity -= current_velocity.normalized() * friction_drop
