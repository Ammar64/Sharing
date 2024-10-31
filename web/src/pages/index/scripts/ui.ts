function createRipple(event: MouseEvent) {
    const button = event.currentTarget as HTMLButtonElement;

    const circle = document.createElement("span");
    const diameter = Math.max(button.clientWidth, button.clientHeight);
    const radius = diameter / 2;

    circle.style.width = circle.style.height = `${diameter}px`;
    circle.style.left = `${event.clientX - button.offsetLeft - radius}px`;
    circle.style.top = `${event.clientY - button.offsetTop - radius}px`;
    circle.className = "ripple";

    const ripple = button.getElementsByClassName("ripple")[0];

    if (ripple) {
        ripple.remove();
    }

    button.appendChild(circle);
    document.addEventListener("mouseup", function(event) {
        removeRipple(button);
    }, { once: true });
}

function removeRipple(button: HTMLButtonElement) {
    const ripple = button.getElementsByClassName("ripple")[0];
    ripple.remove();
}

const buttons = document.getElementsByClassName("ripple-button") as HTMLCollectionOf<HTMLButtonElement>;
for (const button of buttons) {
    button.addEventListener("mousedown", createRipple);
}

const iconButtonsImages = document.querySelectorAll('.ripple-button>img');
for( const image of iconButtonsImages ) {
    image.setAttribute("draggable", "false");
}