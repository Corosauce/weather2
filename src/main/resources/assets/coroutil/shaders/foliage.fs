#version 130

uniform sampler2D texture_sampler;
uniform int fogmode;
//uniform int stipple[64];

varying vec2 outTexCoord;
//flat varying float outBrightness;
varying vec4 outRGBA;
//varying float outAlphaInt;

void main()
{

    //considering range 0.9 to 1.0 is quite costly, and provides minimal visual difference, this is more efficient
    //scratch that, stipple is crazy expensive in many scenarios, damn
    /*if (outRGBA.w < 0.9) {

        ivec2 coord = ivec2(gl_FragCoord.xy - 0.5);

        if (stipple[int(mod(coord.x, 8) + mod(coord.y, 8) * 8)] < outAlphaInt - 1) {
            discard;
        }
    }*/

    float fogFactor = 0;
    if (fogmode == 0) {
        // Linear fog
        fogFactor = (gl_Fog.end - gl_FogFragCoord) * gl_Fog.scale;
    } else if (fogmode == 1) {
        // Exp fog
        fogFactor = exp(-gl_Fog.density * gl_FogFragCoord);
    }

    //0 = full fog
    //1 = no fog

    /*int kk = lightmapColors[i] >> 16 & 255;
    int ll = lightmapColors[i] >> 8 & 255;
    int ii = lightmapColors[i] & 255;*/

    //int lightMap = int(outBrightness);
    //full 1 1 1
    //lightMap = -1;
    //mostly blue
    //lightMap = -13421569;
    /*float r = float((lightMap >> 16) & 255) / 255.0;
    float g = float((lightMap >> 8) & 255) / 255.0;
    float b = float(lightMap & 255) / 255.0;*/

    /*r = 0.2F;
    g = 0.2F;
    b = 1F;*/

    /*float r = 1F;
    float g = 1F;
    float b = 1F;*/

    vec4 fragColor = texture2D(texture_sampler, outTexCoord);
	fragColor.x *= outRGBA.x;
	fragColor.y *= outRGBA.y;
	fragColor.z *= outRGBA.z;
	fragColor.w *= outRGBA.w;

	/*if (stipple[1] == 0) {
	    fragColor.w = 1;
	}*/





	if (outRGBA.w > 0) {
        fogFactor = clamp(fogFactor, 0.0, 1.0);
        gl_FragColor = mix(gl_Fog.color, fragColor, fogFactor);
        gl_FragColor.w = fragColor.w;
        //gl_FragColor = fragColor;
    } else {
        gl_FragColor = fragColor;
    }

    //gl_FragColor = fragColor;
}