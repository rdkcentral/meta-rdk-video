#include <ctype.h>
#include <avmonitor.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>


#define Trace(fmt, ...)                                 \
    do {                                                \
        fprintf(stdout, "<< " fmt "\n", ##__VA_ARGS__); \
        fflush(stdout);                                 \
    } while (0)

void ShowMenu()
{
    printf("Enter\n"
           "\tE : Send sample event\n"
           "\t? : Show help\n"
           );
}

int main()
{
    int16_t result = 0;
    avmonitor_event_t event;
    event.gstCaps = "SAMPLE CAPS";
    result = avmonitor_register_event(event);

    ShowMenu();

    int character;
    do {
        character = toupper(getc(stdin));

        switch (character) {
        case 'E': {
            avmonitor_event_t event;
            event.gstCaps = "SAMPLE CAPS";
            result = avmonitor_register_event(event);
            Trace("Register event result: %d", result);
        }
        case '?': {
            ShowMenu();
            break;
        }
        default:
            break;
        }
    } while (character != 'Q');

    Trace("Done");

    avmonitor_dispose();

    return 0;
}
