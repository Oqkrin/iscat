class_name RotationLib

static func lerp_towards_rad(from: float, to: float, atSpeed: float, delta: float) -> float:
	return lerp_angle(from, to, atSpeed * delta)

static func lerp_towards_deg(from: float, to: float, atSpeed: float, delta: float) -> float:
	return rad_to_deg(lerp_angle(deg_to_rad(from), deg_to_rad(to), atSpeed * delta))