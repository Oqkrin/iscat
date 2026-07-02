extends Node
class_name PlayerInputController

var entity: Entity

func _ready() -> void:
	# Assume this is attached to the Entity or as a child of the Entity
	var parent = get_parent()
	if parent is Entity:
		entity = parent
	else:
		push_warning("PlayerInputController must be a child of an Entity node.")

func _physics_process(delta: float) -> void:
	if not is_instance_valid(entity): return
	
	# Pass rotation target (mouse position) to the entity
	entity.target_rotation_pos = entity.get_global_mouse_position()
	
	# Pass movement input as a generalized direction
	var input_dir := Input.get_vector("player_left", "player_right", "player_up", "player_down")
	entity.move_direction = input_dir
