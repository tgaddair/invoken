// GL ES specific stuff
#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif

//attributes from vertex shader
varying LOWP vec4 vColor;
varying vec2 vTexCoord;

//our texture samplers
uniform sampler2D u_texture;   //diffuse map
uniform sampler2D u_normals;   //normal map
uniform sampler2D u_lights;   //light map
uniform sampler2D u_overlay;   //overlay map

//values used for shading algorithm...
uniform vec2 Resolution;         //resolution of screen
uniform vec3 LightPos;           //light position, normalized
uniform LOWP vec4 LightColor;    //light RGBA -- alpha is intensity
uniform LOWP vec4 AmbientColor;  //ambient RGBA -- alpha is intensity 
uniform vec3 Falloff;            //attenuation coefficients
uniform vec3 lightGeometry[64];
uniform int lightCount;

void main() {
   //RGBA of our diffuse color
   vec4 DiffuseColor = texture2D(u_texture, vTexCoord);
   
   //RGB of our normal map
   vec2 globalCoord = (gl_FragCoord.xy / Resolution.xy);
   vec3 NormalMap = texture2D(u_normals, globalCoord).rgb;
   //vec4 light = texture2D(u_lights, globalCoord);
   //vec4 overlay = texture2D(u_overlay, globalCoord);
   //light = light * (1 - overlay);

   // normalize the normal map
   vec3 N = normalize(NormalMap * 2.0 - 1.0);

   // accumulate light into single vector
   vec3 DiffuseTotal = vec3(0.0, 0.0, 0.0);

   // apply light contributions
   for (int i = 0; i < lightCount; i++) {
     vec3 light = lightGeometry[i];
     vec2 lightCoord = light.xy / Resolution.xy;
     float radius = light.z;

     // the delta position of light
     vec3 LightDir = vec3(lightCoord - globalCoord, LightPos.z);

     // correct for aspect ratio
     LightDir.x *= Resolution.x / Resolution.y;
     
     // determine distance (used for attenuation) BEFORE we normalize LightDir
     float D = length(LightDir);

     if (radius > 0) {
       // normalize our vector
       vec3 L = normalize(LightDir);
       
       // pre-multiply light color with intensity
       // then perform \"N dot L\" to determine our diffuse term
       vec3 Diffuse = (LightColor.rgb * LightColor.a) * max(dot(N, L), 0.0);
       
       // calculate attenuation
       float Attenuation = 1.0 / ( Falloff.x + (Falloff.y*D) + (Falloff.z*D*D) );

       // sum the diffuse and attenuation int the diffuse total
       DiffuseTotal += Diffuse * Attenuation;
     }
   }

   // pre-multiply ambient color with intensity
   vec3 Ambient = AmbientColor.rgb * AmbientColor.a;
   
   // the calculation which brings it all together
   vec3 Intensity = Ambient + DiffuseTotal;
   vec3 FinalColor = DiffuseColor.rgb * Intensity;
   gl_FragColor = vColor * vec4(FinalColor, DiffuseColor.a);
}
