package uni.gaben.iscat.universe.entity.record;

import java.util.List;

public record AudioData(
        List<String> attack,
        List<String> idle,
        List<String> hurt,
        List<String> death,
        List<String> spawn
) {}
