#version 150

#moj_import <light.glsl>

in vec3 Position;
in vec2 UV0;
in mat4 ModelMatrix;
in float Brightness;
in vec4 Color;
//in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord0;
out float vertexDistance;
out vec4 vertexColor;
out vec4 normal;

void main() {
    vec3 Normal = vec3(1, 1, 1);
    vec4 Color2 = vec4(gl_VertexID * 0.1, 1, 1, 1);
    //vec3 Position2 = vec3(gl_VertexID * 20, 0, -gl_VertexID * 20);
    vec3 Position2 = vec3(0, 0, 0);
    if (gl_VertexID == 0) {
        Position2 = vec3(-0.5 + 5, 0.5 + 5, 0.0);
        Color2 = vec4(1, 0, 0, 1);
    } else if (gl_VertexID == 1) {
        Position2 = vec3(-0.5 + 5, -0.5 + 5, 0.0);
        Color2 = vec4(0, 1, 0, 1);
    } else if (gl_VertexID == 2) {
        Position2 = vec3(0.5 + 5, -0.5 + 5, 0.0);
        Color2 = vec4(0, 0, 1, 1);
    } else if (gl_VertexID == 3) {
        Position2 = vec3(0.5 + 5, -0.5 + 5, 0.0);
        Color2 = vec4(1, 1, 0, 1);
    }
    //gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    //gl_Position = vec4(Position, 1.0);
    //gl_Position = vec4(Position2, 1.0);
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    //gl_Position = ProjMat * ModelViewMat * ModelMatrix * vec4(Position, 1.0);
    //gl_Position = ProjMat * ModelViewMat * vec4(Position2, 1.0);

    texCoord0 = UV0;
    //vertexDistance = length((ModelViewMat * vec4(Position, 1.0)).xyz);
    vertexDistance = 5;
    //vertexColor = Color;
    vertexColor = Color2;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}


