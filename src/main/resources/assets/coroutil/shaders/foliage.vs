#version 130
#extension GL_EXT_gpu_shader4 : enable

//in int gl_VertexID;
//in int gl_InstanceID;
//seldom changing or 1 time use data - non instanced:
attribute vec3 position; //mesh pos
attribute vec2 texCoord;
attribute vec3 vertexNormal; //unused
//seldom - instanced
attribute mat4 modelMatrix; //used to be modelViewMatrix, separate from view matrix
attribute vec4 rgba; //4th entry, alpha not used here, might as well leave vec4 unless more efficient to separate things to per float/attrib entries
attribute vec4 meta;
//often changed data - instanced
attribute vec2 alphaBrightness;

varying vec2 outTexCoord;
//flat varying float outBrightness;
varying vec4 outRGBA;
varying float outAlphaInt;

uniform mat4 modelViewMatrixCamera;

uniform int time;
uniform float partialTick;
uniform float windDir;
uniform float windSpeed;

vec3 computeCorner(vec3 sway, vec3 angle, vec3 center) {
    return center + normalize(cross(sway, angle)) * 0.5;
}

mat4 rotationMatrix(vec3 axis, float angle) {
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
}

void main()
{

    float radian = 0.0174533;
    int swayLag = 20;
    float index = meta.x;
    float animationID = meta.y;
    float heightIndex = meta.z;
    float antiStiffness = meta.w;
    float rotation = rgba.w;

    float baseTimeChangeRate = 60.0 * windSpeed;
    float timeSmooth = (time-baseTimeChangeRate) + (baseTimeChangeRate * partialTick);
    timeSmooth += index * 200;

    vec3 pos = vec3(0, 0, 0);

    mat4 finalMat = modelViewMatrixCamera * modelMatrix;
    vec3 posTestAdj = position;
    posTestAdj.y = posTestAdj.y + heightIndex + 0.5;
    vec4 posTest = finalMat * vec4(posTestAdj.x, posTestAdj.y, posTestAdj.z, 1.0);

    if (windSpeed > 0.00001 && posTest.w < 999) {

        //wind hit foliage, 1 high for now
        if (animationID == 0) {

            //BETTER CODE START

            float variance = 0.6;

            //try offsetting mesh so bottom is 0
            vec3 usePos = position;
            usePos.y = usePos.y + 0.5;

            float heightFromBase = heightIndex + usePos.y;

            swayLag = int(heightFromBase * -baseTimeChangeRate * 0.2);
            //swayLag = int(heightFromBase * -1);

            float windSpeedAdj = windSpeed * 0.05 * (heightFromBase);

            windSpeedAdj = windSpeedAdj * (antiStiffness * 2.0);

            //a bit of hack to make all but reeds be influenced by wind more lower down
            if (antiStiffness == 1.0) {
                windSpeedAdj = windSpeed * 0.5;
            }

            //disable for more variance per height
            //windSpeedAdj = windSpeed * 0.2;

            float adjDir = windDir/* - rotation*/;

            vec3 windAdj = vec3(-sin(adjDir * radian) * windSpeedAdj, 0, cos(adjDir * radian) * windSpeedAdj);

            float yAdj = windAdj.y;
            if (antiStiffness == 1.0) {
                //yAdj = cross(windAdj, vec3(1, 0, 1)).y;
            }

            //semi hacky fix for rotation being done before we apply sway logic
            if (rotation == 45.0) {
                windAdj = vec3(-cos(adjDir * radian) * windSpeedAdj, 0, -sin(adjDir * radian) * windSpeedAdj);
            }

            //maybe correct, added gap between mesh connections though
            if (antiStiffness == 1.0) {
                //windAdj.y = yAdj;
            }

            //windAdj.y = windAdj.y - 0.2;

            //this.rotationYaw is quaternion is both required but messing with the sway math, rework when its quat rotated?
            //timeModTop = int(mod((((timeSmooth + ((1) * swayLag))) * 60.0 * windSpeed), 360));
            //timeModTop = int(mod((((timeSmooth + ((1) * swayLag))) * 60.0), 360));
            //timeModTop = int((((timeSmooth + ((0.001) * swayLag))) * 1.0));
            int timeModTop = int(mod((timeSmooth * 0.2/* * antiStiffness*/) + swayLag, 360));
            //timeModTop = int(mod(int(timeSmooth * windSpeed * 10), 360));
            //timeModTop = int(mod(90, 360));

            variance = 0.02 + (0.05 * windSpeed);

            variance = variance * antiStiffness;

            //enable for more variance per height
            //variance = 0.06 * (heightFromBase * heightFromBase * 0.02);
            vec3 chaosAdj = vec3(-sin(timeModTop * radian) * variance, 0, cos(timeModTop * radian) * variance);

            //semi hacky fix for rotation being done before we apply sway logic
            if (rotation == 45.0) {
                chaosAdj = vec3(-cos(timeModTop * radian) * variance, 0, -sin(timeModTop * radian) * variance);
            }

            windAdj = windAdj * heightFromBase;
            chaosAdj = chaosAdj * heightFromBase * 1.0;

            pos = usePos;

            pos = pos + windAdj + chaosAdj;
            pos.y = pos.y + heightIndex;

        //seaweed
        } else if (false && animationID == 1) {

            //timeSmooth = 1;

            float variance = 0.6;

            vec3 angle = vec3(-1, 0, 1);
            if (rotation == 1) {
                angle = vec3(1, 0, 1);
            }

            //more performant but less accurate algorithm, use unless crazy mesh warping needed
            vec3 baseHeight = vec3(0, heightIndex-1, 0);
            vec3 baseHeight2 = vec3(0, heightIndex, 0);

            int timeModBottom = int(mod(((timeSmooth + ((heightIndex - 1 + 1) * swayLag)) * 2) + rotation, 360));
            vec3 swayBottom = vec3(sin(timeModBottom * radian) * variance, 1, cos(timeModBottom * radian) * variance);
            vec3 prevSway = swayBottom;
            vec3 bottom = baseHeight + swayBottom;

            int timeModTop = int(mod(((timeSmooth + ((heightIndex + 1) * swayLag)) * 2) + rotation, 360));
            vec3 sway = vec3(sin(timeModTop * radian) * variance, 1, cos(timeModTop * radian) * variance);
            vec3 top = baseHeight2 + sway;
            if (heightIndex == 0) {
                bottom = vec3(0, 0, 0);
                prevSway = vec3(0, 1, 0);
            }

            //more accurate but more expensive loop
            /*

            vec3 top = vec3(0, 0, 0);
            vec3 bottom = vec3(0, 0, 0);
            vec3 bottomNext = bottom;
            //verify
            vec3 sway = vec3(0, 0, 0);

            for (int i = 0; i <= heightIndex; i++) {
                prevSway = sway;
                timeMod = int(mod(((timeSmooth + ((i + 1) * swayLag)) * 2) + rotation, 360));
                sway = vec3(sin(timeMod * radian) * variance, 1, cos(timeMod * radian) * variance);
                sway = normalize(sway);

                top = bottomNext + sway;

                bottom = bottomNext;
                bottomNext = top;
            }*/

            if (gl_VertexID == 0) {
                pos = computeCorner(sway, angle, top);
            } else if (gl_VertexID == 1) {
                pos = computeCorner(prevSway, angle, bottom);
            } else if (gl_VertexID == 2) {
                angle = angle * -1;
                pos = computeCorner(prevSway, angle, bottom);
            } else if (gl_VertexID == 3) {
                angle = angle * -1;
                pos = computeCorner(sway, angle, top);
            }
        }

        gl_Position = finalMat * vec4(pos.x, pos.y, pos.z, 1.0);
    } else {
        gl_Position = posTest;//finalMat * vec4(posTestAdj.x, posTestAdj.y, posTestAdj.z, 1.0);
    }




    //lazy, cheap dist to camera
    gl_FogFragCoord = abs(gl_Position.z);

	outTexCoord = texCoord;

	//outBrightness = alphaBrightness.y;
	int lightMap = int(alphaBrightness.y);
    vec3 texMap = vec3(float((lightMap >> 16) & 255) / 255.0, float((lightMap >> 8) & 255) / 255.0, float(lightMap & 255) / 255.0);

	outRGBA = vec4(rgba.x * texMap.x, rgba.y * texMap.y, rgba.z * texMap.z, alphaBrightness.x);
	//outAlphaInt = 255 - int(outRGBA.w * 255);
}