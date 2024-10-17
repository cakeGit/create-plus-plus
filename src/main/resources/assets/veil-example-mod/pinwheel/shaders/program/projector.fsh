#include veil:deferred_utils

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;

uniform sampler2D ProjectionDepthSampler;
uniform sampler2D ProjectionResults;

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

bool isInProjectionSpace(vec2 projectorSpace) {
    return (projectorSpace.x > 0.005f && projectorSpace.y > 0.005f && projectorSpace.x < 0.995f && projectorSpace.y < 0.995f);
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
            total += texture(sampler, uv + projectorOneTexel * vec2(x, y) * 1f).r;
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

                float projectorDepth = depthTextureRadius(ProjectionDepthSampler, projectorUV, projectorOneTexel, 0);
                float currentDepth = toDepthInProjectorDepthSpace(samplePos);
//                float uncertainty =  0.0001f;

                if (currentDepth - projectorDepth < uncertainty)
                    total += max(abs(currentDepth - projectorDepth) / uncertainty, 1f);
                count++;
            }
        }
    }

    return total / count;
}

uniform int isFirstLayer;

void main() {
    vec4 original = vec4(0);

    if (isFirstLayer == 0) {
        original = texture(ProjectionResults, texCoord);
    }

    float depth = texture(DiffuseDepthSampler, texCoord).r;

    vec3 pos = viewToWorldSpace(viewPosFromDepth(depth, texCoord));

    vec2 projectorSpace = toProjectorDepthSpace(pos);

    if (isInProjectionSpace(projectorSpace) && dot(direction, normalize(pos - origin)) > 0.0f) {
        float distanceFromOrigin = length(pos - origin);
        float uncertainty = 0.001f / distanceFromOrigin;

        float strength = getAverageStrengthOfRadius(pos, 1, uncertainty) * 2f * (inversesqrt(distanceFromOrigin));

//        float currentDarkness = 1f/length(original);

        strength = strength * 100;
        strength = min(strength, 50);

        if (strength > 0f)
            fragColor = max(original, original + (0.1f * vec4(0.04f, 0.02f, 0.1f, 1.0f) * strength));
        else
            fragColor = original;
//        fragColor = texture(ProjectionDepthSampler, projectorSpace) * 2f;
    } else {
        fragColor = original;
    }

}
