#version 130

uniform sampler2D texture_sampler;
uniform int fogmode;

varying vec2 outTexCoord;
//varying float outBrightness;
varying vec4 outRGBA;

void main()
{

    float fogFactor = 0;


    if (fogmode == 0) {
        // Linear fog
        fogFactor = (gl_Fog.end - gl_FogFragCoord) * gl_Fog.scale;
    } else if (fogmode == 1) {
        // Exp fog
        fogFactor = exp(-gl_Fog.density * gl_FogFragCoord);
    }

    vec4 fragColor = texture2D(texture_sampler, outTexCoord);
	fragColor.x *= outRGBA.x;
	fragColor.y *= outRGBA.y;
	fragColor.z *= outRGBA.z;
	fragColor.w *= outRGBA.w;

    if (outRGBA.w > 0) {
        fogFactor = clamp(fogFactor, 0.0, 1.0);
        gl_FragColor = mix(gl_Fog.color, fragColor, fogFactor);
        gl_FragColor.w = fragColor.w;
        //gl_FragColor = fragColor;
    } else {
        gl_FragColor = fragColor;
    }

    //gl_FragColor.w = 0.1;

    //gl_FragColor = fragColor;
}