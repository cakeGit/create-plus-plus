#include veil:deferred_utils

uniform sampler2D DiffuseSampler0;
in vec2 texCoord;

uniform vec3 origin;
uniform float aspect;

out vec4 fragColor;

vec2 projectToScreenTex(vec3 worldPosition) {
    vec3 relative = worldPosition - VeilCamera.CameraPosition;
    vec4 clipSpacePos = VeilCamera.ProjMat * (VeilCamera.ViewMat * vec4(relative, 1.0f));
    vec2 screen = (clipSpacePos.xyz / clipSpacePos.w).xy;
    return (screen + vec2(1.0f, 1.0f)) / vec2(2.0f, 2.0f);
}

vec2 aspectScale() {
    return vec2(
    max(aspect, 1f),
    min(aspect, 1f)
    );
}

void main() {
    vec4 original = texture(DiffuseSampler0, texCoord);
    fragColor = original;
//
//    vec2 originProjected = projectToScreenTex(origin);
//
//    vec2 originFocusProjected = projectToScreenTex(origin + vec3(0.0f, -0.1f, 0.0f));
//
//    float worldDistance = length(origin - VeilCamera.CameraPosition);
//
//    vec2 circleVector = (originProjected - texCoord) * aspectScale();
//    float viewDistance = length(circleVector / 2f);
//
//    float maxViewDistance = 0.1f / (worldDistance * worldDistance);
//
//    float fadeOut;
//    if (worldDistance < 10)
//    fadeOut = 1f;
//    else
//    fadeOut = max(1 - pow((worldDistance / 5f) - 10, 2), 0f);
//
//    float strength = (maxViewDistance - viewDistance);
//
//    strength = strength * fadeOut;
//    strength *= (worldDistance * worldDistance * 3f);
//    strength = clamp(strength, 0, 0.2);
//
//    if (strength > 0f) {
//        fragColor += vec4(0.08f, 0.05f, 0.2f, 1.0f) * 20f * strength;
//    }

}

//vec2 originProjected = projectToScreenTex(origin);
//
//vec2 originFocusProjected = projectToScreenTex(origin + vec3(0.0f, -0.1f, 0.0f));
//
//float worldDistance = length(origin - VeilCamera.CameraPosition);
//
//vec2 relative = (originProjected - texCoord) * aspectScale();
//
//vec2 normalizedRelative = normalize(relative);
//
//vec2 effectiveRadius = vec2(
//relative.x / (normalizedRelative.y * normalizedRelative.y * 10),
//relative.y / (normalizedRelative.x * normalizedRelative.x * 10)
//);
//
//float viewDistance = length(effectiveRadius / 2f);
//
//float maxViewDistance = 0.1f / (worldDistance * worldDistance);
//
//float fadeOut;
//if (worldDistance < 10)
//fadeOut = 1f;
//else
//fadeOut = max(1 - pow((worldDistance / 5f) - 10, 2), 0f);
//
//float strength = (maxViewDistance - viewDistance);
//
//strength = strength * fadeOut;
//strength *= (worldDistance * worldDistance * 3f);
//strength = clamp(strength, 0, 0.2);
