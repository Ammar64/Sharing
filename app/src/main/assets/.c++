#define __ANDROID__
#include <SDL2/SDL.h>


int main() {
    SDL_RWops *rw = SDL_RWFromFile("file.txt", "r");
    
    if(rw) {
        // get the size of the file
        Sint64 size = SDL_RWsize(rw);

        // read the file into a buffer
        char *buffer = new char[size];
        SDL_RWread(rw, buffer, sizeof(char), size);
        // don't forget to close the file
        SDL_RWclose(rw);


        // do whatever you want to do with file data but don't forget to deallocate the buffer
        delete[] buffer;
    }

    
    
}