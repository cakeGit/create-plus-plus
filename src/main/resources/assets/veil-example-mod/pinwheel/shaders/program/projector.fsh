#include veil:deferred_utils

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;

uniform sampler2D ProjectionDepthSampler;

in vec2 texCoord;

uniform vec3 origin;
uniform vec3 direction;

uniform mat4 DepthMatrix;
uniform float BaseAspect;

uniform float ProjectorPlaneNear;
uniform float ProjectorPlaneFar;

uniform vec2 projectorOneTexel;

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


float projectorDepthSampleToWorldDepth(float depthSample) {
    float f = depthSample * 2.0 - 1.0;
    return 2.0 * ProjectorPlaneNear * ProjectorPlaneFar / (ProjectorPlaneFar + ProjectorPlaneNear - f * (ProjectorPlaneFar - ProjectorPlaneNear));
}

void main() {
    vec4 original = texture(DiffuseSampler0, texCoord);
    float depth = texture(DiffuseDepthSampler, texCoord).r;

    vec3 pos = viewToWorldSpace(viewPosFromDepth(depth, texCoord));

    vec2 projectorSpace = toProjectorDepthSpace(pos);

    if (isInProjectionSpace(projectorSpace) && dot(pos, direction) > 0f) {

        float depthMax = -1;

        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                float current = texture(ProjectionDepthSampler, projectorSpace + projectorOneTexel * vec2(x, y)).r;
                if (depthMax == -1)
                    depthMax = current;
                else
                    depthMax = min(current, depthMax);
            }
        }

        float depth = (depthMax)*128f;

        //        float worldDepth = projectorDepthSampleToWorldDepth(depth);

        float deltaDepth = abs(pos.y) - depth;

        float strength = clamp(1f - ((deltaDepth*deltaDepth) / 0.1f), 0f, 1f);

        if (strength == 0f)
            fragColor = original;
        else
            fragColor = original + vec4(0.08f, 0.05f, 0.2f, 1.0f) * strength;
    } else
    fragColor = original;

}
