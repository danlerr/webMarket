document.addEventListener("DOMContentLoaded", function () {
    let notifiche = document.querySelectorAll(".notifica-btn");

    notifiche.forEach(bottone => {
        bottone.addEventListener("click", function () {
            let tipo = this.getAttribute("data-tipo"); // Ottiene il tipo di notifica (richieste, proposte, ordini)
            let pallino = document.querySelector(".notifica-" + tipo);

            if (pallino) {
                pallino.style.display = "none"; // Nasconde il pallino dopo il click
            }
        });
    });
});