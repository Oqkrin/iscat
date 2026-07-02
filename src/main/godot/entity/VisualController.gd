class_name VisualController
extends RefCounted

var sprite: AnimatedSprite2D
var settings: VisualData

func _init(target_sprite: AnimatedSprite2D, config: VisualData) -> void:
	sprite = target_sprite
	settings = config
	
	if is_instance_valid(sprite):
		sprite.scale = settings.base_scale

func play_movement_animation(is_moving: bool) -> void:
	if not is_instance_valid(sprite): return
	
	if is_moving:
		sprite.play(settings.default_move_anim)
	else:
		sprite.play(settings.default_idle_anim)
