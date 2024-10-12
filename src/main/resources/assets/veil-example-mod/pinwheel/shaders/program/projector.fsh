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

uniform mat4 DepthMat;
uniform mat4 DepthModelMat;

uniform vec2 projectorOneTexel;

out vec4 fragColor;
//TODO Remove fpe clip
bool isInProjectionSpace(vec2 projectorSpace) {
    return (projectorSpace.x > 0.0f && projectorSpace.y > 0.0f && projectorSpace.x < 1.0f && projectorSpace.y < 1.0f);
    //    return true;
}

float toDepthInProjectorDepthSpace(vec3 worldPosition) {
    vec3 relative = worldPosition - origin;

    vec4 clipSpacePos = (DepthMat * vec4(relative, 1.0f));

    float projectorSpace = (clipSpacePos.xyz / clipSpacePos.w).z;

    return (projectorSpace + 1.0f) / 2.0f;
}


vec2 toProjectorDepthSpace(vec3 worldPosition) {
    vec3 relative = worldPosition - origin;

    vec4 clipSpacePos = (DepthMat * vec4(relative, 1.0f));

    vec2 projectorSpace = (clipSpacePos.xyz / clipSpacePos.w).xy;

    return (projectorSpace + vec2(1.0f, 1.0f)) / vec2(2.0f, 2.0f);
}


float projectorDepthSampleToWorldDepth(float depthSample) {
    float f = depthSample * 2.0 - 1.0;
    return 2.0 * ProjectorPlaneNear * ProjectorPlaneFar / (ProjectorPlaneFar + ProjectorPlaneNear - f * (ProjectorPlaneFar - ProjectorPlaneNear));
}

vec3 projectorPosFromDepth(float depth, vec2 uv) {
    vec4 positionCS = vec4(uv, depth, 1.0) * 2.0 - 1.0;
    vec4 positionVS = DepthMat * positionCS;
    positionVS /= positionVS.w;

    return positionVS.xyz;
}

vec3 projectorViewToWorldSpace(vec3 positionVS) {
    return origin + positionVS;
}

float depthTextureRadius(sampler2D sampler, vec2 uv, vec2 projectorOneTexel, int radius) {
    float total = 0f;
    int count = 0;
    for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
            total += texture(sampler, uv + projectorOneTexel * vec2(x, y)).r;
            count++;
        }
    }
    return total / count;
}

float getAverageStrengthOfRadius(vec3 pos, int radius, float uncertainty) {
    float total = 0f;
    int count = 0;
    for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
                vec3 samplePos = pos + vec3(x, y, z) * (length(pos - origin) / 1000);

                vec2 projectorUV = toProjectorDepthSpace(samplePos);

                float projectorDepth = depthTextureRadius(ProjectionDepthSampler, projectorUV, projectorOneTexel, 1);
                float currentDepth = toDepthInProjectorDepthSpace(samplePos);

                if (currentDepth - projectorDepth < uncertainty)
                    total += 1f;
                count++;
            }
        }
    }

    return total / count;
}

void main() {
    vec4 original = texture(DiffuseSampler0, texCoord);
    float depth = texture(DiffuseDepthSampler, texCoord).r;

    vec3 pos = viewToWorldSpace(viewPosFromDepth(depth, texCoord));

    vec2 projectorSpace = toProjectorDepthSpace(pos);

    if (isInProjectionSpace(projectorSpace) && dot(direction, normalize(pos - origin)) > 0.0f) {
        float distanceFromOrigin = length(pos - origin);
        float uncertainty = 0.0001f / distanceFromOrigin;

        float strength = getAverageStrengthOfRadius(pos, 2, uncertainty) * 2f * (inversesqrt(distanceFromOrigin));

        strength = strength * 100;
        strength = min(strength, 50);

        if (strength > 0f)
            fragColor = original + (original * vec4(0.04f, 0.02f, 0.1f, 1.0f) * strength);
        else
            fragColor = original;
    } else {
        fragColor = original;
    }

}
