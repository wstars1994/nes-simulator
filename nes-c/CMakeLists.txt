cmake_minimum_required(VERSION 3.22)
project(nes_c)

set(CMAKE_C_STANDARD 99)

include_directories(include)
link_directories(lib/x64)

add_executable(nes_c main.c cpu6502.c ppu.c nes.c mem.c render.c)

target_link_libraries(nes_c libopengl32.a  libfreeglut.a)