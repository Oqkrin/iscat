class_name RotationController
extends RefCounted

var parent: Node2D
var angular_velocity: float

func _init(node: Node2D, velocity: float = 7.0) -> void:
	parent = node
	angular_velocity = velocity

func get_rad_towards_pos(target_pos: Vector2, delta: float, offset: float = PI / 2) -> float:
	if not is_instance_valid(parent): 
		return 0.0 
	var target_angle: float = parent.global_rotation + parent.get_angle_to(target_pos) + offset
	return RotationLib.lerp_towards_rad(parent.global_rotation, target_angle, angular_velocity, delta)

func get_deg_towards_pos(target_pos: Vector2, delta: float, offset: float = 90.0) -> float:
	if not is_instance_valid(parent): 
		return 0.0
	var target_angle: float = parent.global_rotation_degrees + rad_to_deg(parent.get_angle_to(target_pos)) + offset
	return RotationLib.lerp_towards_deg(parent.global_rotation_degrees, target_angle, angular_velocity, delta)
