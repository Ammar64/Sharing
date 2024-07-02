document.addEventListener("DOMContentLoaded", function() {
    const carousel = document.querySelector(".carousel");
    const arrowBtns = document.querySelectorAll(".btn");
    const wrapper = document.querySelector(".wrapper");
    const dots = document.querySelectorAll(".dot");
    const lastBtn = document.querySelector(".lastBtn");

    const firstCard = carousel.querySelector(".card");
    const firstCardWidth = firstCard.offsetWidth + 20; // Adjust for gap

    let timeoutId,
        currentIndex = 0;

    const updateDots = () => {
        const newIndex = Math.round(carousel.scrollLeft / firstCardWidth);
        if (newIndex !== currentIndex) {
            currentIndex = newIndex;
            dots.forEach((dot, idx) => {
                dot.classList.toggle("active", idx === currentIndex);
            });
            updateCards();
        }
    };

    const updateCards = () => {
        const cards = carousel.querySelectorAll(".card");
        cards.forEach((card, idx) => {
            card.classList.toggle("active", idx === currentIndex);
            card.classList.toggle("inactive", idx !== currentIndex);
        });
    };

    const handleArrowClick = (direction) => {
        const maxIndex = dots.length - 1;

        // Handle end condition
        if (currentIndex === maxIndex && direction === 1) {
            AndroidNativeInterface.endTutorial();
        }

        currentIndex = Math.min(Math.max(currentIndex + direction, 0), maxIndex);
        carousel.scrollBy({
            left: direction * firstCardWidth,
            behavior: "smooth"
        });
        updateDots();
        updateCards();
    };

    const handleDotClick = (index) => {
        currentIndex = index;
        carousel.scrollTo({
            left: index * firstCardWidth,
            behavior: "smooth"
        });
        updateDots();
        updateCards();
    };

    carousel.addEventListener("scroll", updateDots);

    arrowBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            const direction = btn.classList.contains("left") ? -1 : 1;
            handleArrowClick(direction);
        });
    });

    dots.forEach(dot => {
        dot.addEventListener("click", () => {
            const index = parseInt(dot.getAttribute("data-index"));
            handleDotClick(index);
        });
    });

    lastBtn.onclick = () => {
        AndroidNativeInterface.endTutorial();
    };

    updateCards();

    // Prevent swipe on touch devices
    let startX;
    let startY;

    carousel.addEventListener('touchstart', (e) => {
        startX = e.touches[0].clientX;
        startY = e.touches[0].clientY;
    });

    carousel.addEventListener('touchmove', (e) => {
        const deltaX = e.touches[0].clientX - startX;
        const deltaY = e.touches[0].clientY - startY;

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            e.preventDefault(); // Prevent horizontal swipe
        }
    });
});

function setVhProperty() {
    var vh = window.innerHeight * 0.01;
    document.documentElement.style.setProperty('--vh', `${vh}px`);
}

// Set the initial value
setVhProperty();

// Update the value on resize
window.addEventListener('resize', setVhProperty);
