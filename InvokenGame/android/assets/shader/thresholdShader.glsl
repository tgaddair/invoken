#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 vColor;
varying vec2 vTexCoord;

//our texture samplers
uniform sampler2D u_texture; //diffuse map

void main() {
	vec4 DiffuseColor = texture2D(u_texture, vTexCoord);

  // set the pixel color to 1 if not black or 0 if black
  float intensity = DiffuseColor.x + DiffuseColor.y + DiffuseColor.z;
  DiffuseColor.x = DiffuseColor.y = DiffuseColor.z = max(intensity, 1);
	gl_FragColor = vColor * DiffuseColor;
}
