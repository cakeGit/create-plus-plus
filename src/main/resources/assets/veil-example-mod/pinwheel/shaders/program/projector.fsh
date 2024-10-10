#include veil:deferred_utils

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;

uniform sampler2D ProjectionDepthSampler;

in vec2 texCoord;

uniform vec3 origin;
uniform vec3 direction;

uniform mat4 DepthMatrix;
uniform float BaseAspect;

out vec4 fragColor;

bool isInProjectionSpace(vec2 projectorSpace) {
    return (projectorSpace.x > 0.001f && projectorSpace.y > 0.001f && projectorSpace.x < 0.999f && projectorSpace.y < 0.999f);
}

vec2 toProjectorDepthSpace(vec3 worldPosition) {
    vec3 relative = worldPosition - origin;

    vec2 projectorSpace = (DepthMatrix * vec4(relative, 1.0f)).xy;

//    projectorSpace.xy /= worldPosition.y / 10f;

    return (projectorSpace + vec2(1.0f, 1.0f)) / vec2(2.0f, 2.0f);
}

void main() {
    vec4 original = texture(DiffuseSampler0,texCoord);
    float depth = texture(DiffuseDepthSampler, texCoord).r;

    vec3 pos = viewToWorldSpace(viewPosFromDepth(depth, texCoord));

    vec2 projectorSpace = toProjectorDepthSpace(pos);

    if (isInProjectionSpace(projectorSpace) && dot(pos, direction) > 0f)
        fragColor = texture(ProjectionDepthSampler, projectorSpace);
    else
        fragColor = original;

}
