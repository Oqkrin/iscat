class_name VisualData
extends Resource

@export var sprite_offset_rad: float = PI / 2
@export var base_scale: Vector2 = Vector2(1, 1)

@export_category("Animations")
@export var default_idle_anim: String = "default"
@export var default_move_anim: String = "move"
@export var default_attack_anim: String = "attack"