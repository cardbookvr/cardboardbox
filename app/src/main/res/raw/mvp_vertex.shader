uniform mat4 u_MVP;
attribute vec4 a_Position;
void main() {
   gl_Position = u_MVP * a_Position;
}
