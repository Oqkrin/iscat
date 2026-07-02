extends Node
class_name BrainController

var entity: Entity

func _ready() -> void:
	# Assume this is attached to the Entity or as a child of the Entity
	var parent = get_parent()
	if parent is Entity:
		entity = parent
	else:
		push_warning("BrainController must be a child of an Entity node.")

func _physics_process(delta: float) -> void:
	if not is_instance_valid(entity): return
	
	# Find the player using Godot's group system
	var players = get_tree().get_nodes_in_group("Player")
	if players.is_empty():
		# No player found, stop moving
		entity.move_direction = Vector2.ZERO
		return
	
	var player: Node2D = players[0]
	var player_pos = player.global_position
	
	# 1. Rotate to look at the player
	entity.target_rotation_pos = player_pos
	
	# 2. Steer (move) towards the player
	# This generates a normalized direction vector pointing directly at the player
	var dir_to_player = (player_pos - entity.global_position).normalized()
	entity.move_direction = dir_to_player
