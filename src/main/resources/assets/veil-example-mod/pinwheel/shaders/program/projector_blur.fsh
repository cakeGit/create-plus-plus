out vec4 fragColor;
in vec2 texCoord;

uniform sampler2D DiffuseSampler0;
uniform vec2 InSize;

void main() {
    vec4 original = texture(DiffuseSampler0, texCoord);

    int radius = 40;
    int step = 5;
    vec4 totalColor = vec4(0f);
    int count = 0;

    vec2 oneTexel = (1f / InSize);

    for (int x = -radius; x <= radius; x += step) {
        for (int y = -radius; y <= radius; y += step) {
            totalColor += texture(DiffuseSampler0, texCoord + vec2(x, y) * oneTexel);
            count++;
        }
    }

    fragColor = min(original + (totalColor / count) * 0.5f, vec4(1f));
}