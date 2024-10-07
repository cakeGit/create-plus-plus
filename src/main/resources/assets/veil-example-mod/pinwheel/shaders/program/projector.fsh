uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;

layout(std140) uniform CameraMatrices {
    mat4 ProjMat;
    mat4 IProjMat;
    mat4 ViewMat;
    mat4 IViewMat;
    mat3 IViewRotMat;
    vec3 CameraPosition;
    float NearPlane;
    float FarPlane;
} VeilCamera;

in vec2 texCoord;

uniform float seteone;

uniform vec3 origin;
uniform vec3 direction;

out vec4 fragColor;

vec3 viewPosFromDepthSample(float depth, vec2 uv) {
    vec4 positionCS = vec4(uv, depth, 1.0f) * 2.0f - 1.0f;
    vec4 positionVS = VeilCamera.IProjMat * positionCS;
    positionVS /= positionVS.w;
    return positionVS.xyz;
}

vec3 viewToWorldSpace(vec3 positionVS) {
    return VeilCamera.CameraPosition + (VeilCamera.IViewMat * vec4(positionVS, 1.0f)).xyz;
}//TODO: render from the perspective of origin, to get a depthmap, match with orig

bool isInProjectionSpace(vec3 position) {
    if (dot(normalize(position - origin), direction) > 0.9f) {
        return true;
    }
    return false;
}

void main() {

    vec4 original = texture(DiffuseSampler0,texCoord);

    float depthSample = texture(DiffuseDepthSampler, texCoord).r;
    vec3 pos = viewToWorldSpace(viewPosFromDepthSample(depthSample, texCoord));
    if (isInProjectionSpace(pos)) {
        fragColor = vec4(1f, original.y, original.z, 1f);
    } else
        fragColor = original;

}
