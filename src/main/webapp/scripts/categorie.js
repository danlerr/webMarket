document.addEventListener("DOMContentLoaded", function() {
    // Container dove generiamo dinamicamente i <select> per le categorie
    const subcategoryContainer = document.getElementById("subcategory-container");
    // Container per le caratteristiche
    const characteristicsGroup = document.getElementById("characteristics-group");
    const characteristicsContainer = document.getElementById("characteristics-container");

    // Hidden input che contiene l'ID della categoria finale selezionata
    const finalCategoryInput = document.getElementById("finalCategoryId");

    // Controllo validità per attivare/disattivare il pulsante di submit
    function checkFormValidity() {
        if (finalCategoryInput.value !== "") {
            document.getElementById("submitButton").disabled = false;
        } else {
            document.getElementById("submitButton").disabled = true;
        }
    }

    // Funzione per caricare dinamicamente le *sotto-categorie* di una data categoria
    function loadSubcategories(parentCategoryId, levelSelect) {
        fetch("creaRichiesta?action=getSubcategories&parentCategoryId=" + parentCategoryId)
            .then(response => {
                if(!response.ok) {
                    throw new Error("Errore di rete: " + response.status);
                }
                return response.json();
            })
            .then(data => {
                if (data && data.length > 0) {
                    // Se abbiamo sotto-categorie, creiamo un nuovo <select> e lo appendiamo
                    createSubcategorySelect(data, levelSelect);
                    // Poiché non siamo a un nodo foglia, puliamo le caratteristiche
                    characteristicsContainer.innerHTML = "";
                    characteristicsGroup.style.display = "none";
                    // Reset del finalCategoryId
                    finalCategoryInput.value = "";
                } else {
                    // Se non ci sono sotto-categorie, allora "parentCategoryId" è un nodo foglia:
                    // Salviamo il suo ID come categoria selezionata
                    finalCategoryInput.value = parentCategoryId;
                    // Carichiamo le caratteristiche
                    loadCaratteristiche(parentCategoryId);
                }
                checkFormValidity();
            })
            .catch(error => {
                console.error("Errore nel recupero delle sottocategorie:", error);
                
            });
    }

    // Funzione per caricare le caratteristiche di una categoria foglia
    function loadCaratteristiche(categoryId) {
        fetch("creaRichiesta?action=getCaratteristiche&subcategoryId=" + categoryId)
            .then(response => {
                if(!response.ok){
                    throw new Error("Errore di rete: " + response.status);
                }
                return response.json();
            })
            .then(data => {
                characteristicsContainer.innerHTML = ""; // Pulizia
                if (data && data.length > 0) {
                    data.forEach(caratt => {
                        const div = document.createElement("div");
                        div.className = "mb-3";

                        const label = document.createElement("label");
                        label.className = "form-label";
                        label.textContent = caratt.nome;

                        const input = document.createElement("input");
                        input.type = "text";
                        input.className = "form-control";
                        input.name = "caratteristica-" + caratt.key;

                        div.appendChild(label);
                        div.appendChild(input);
                        characteristicsContainer.appendChild(div);
                    });
                    characteristicsGroup.style.display = "block";
                } else {
                    characteristicsGroup.style.display = "none";
                }
                checkFormValidity();
            })
            .catch(error => {
                console.error("Errore nel recupero delle caratteristiche:", error);
                
            });
    }

    // Crea dinamicamente un nuovo <select> popolato con la lista di subcategorie
    // e lo appende *dopo* levelSelect (o nel container se levelSelect è null)
    function createSubcategorySelect(subcategories, levelSelect) {
        // Se c'è un <select> successivo al current, rimuovilo (e rimuovi tutti quelli ancora successivi).
        removeSelectsAfter(levelSelect);

        
        const wrapperDiv = document.createElement("div");
        wrapperDiv.className = "mb-3";

        const label = document.createElement("label");
        label.textContent = "Sottocategoria:";
        label.className = "form-label";

        const newSelect = document.createElement("select");
        newSelect.className = "form-select";
        newSelect.innerHTML = `<option value="">Seleziona...</option>`;

        // Riempi il <select> con le subcategories
        subcategories.forEach(sc => {
            const opt = document.createElement("option");
            opt.value = sc.key;
            opt.textContent = sc.nome;
            newSelect.appendChild(opt);
        });

        // Al cambiare del valore, ricarichiamo i livelli successivi (o le caratteristiche, se non ce ne sono)
        newSelect.addEventListener("change", () => {
            const selectedId = newSelect.value;
            if (selectedId) {
                // Carichiamo le sottocategorie di "selectedId"
                loadSubcategories(selectedId, newSelect);
            } else {
                // Se l'utente ha deselezionato, pulisci i successivi e resetta finalCategoryId
                removeSelectsAfter(newSelect);
                finalCategoryInput.value = "";
                characteristicsContainer.innerHTML = "";
                characteristicsGroup.style.display = "none";
            }
            checkFormValidity();
        });

        wrapperDiv.appendChild(label);
        wrapperDiv.appendChild(newSelect);

        // Se levelSelect è null, siamo al primissimo livello (la categoria principale)
        if (levelSelect === null) {
            // Append in subcategoryContainer
            subcategoryContainer.innerHTML = ""; // Pulisci
            subcategoryContainer.appendChild(wrapperDiv);
        } else {
            // Altrimenti, inseriscilo dopo il <select> attuale
            // Trova la parent di levelSelect (il wrapper) e poi inserisci dopo
            subcategoryContainer.appendChild(wrapperDiv);
        }
    }

    // Funzione per rimuovere tutti i <select> che si trovano *dopo* quello passato come parametro
    // (utile se l'utente cambia una selezione a un livello intermedio)
    function removeSelectsAfter(currentSelect) {
        // Prendi tutti i wrapper <div> che contengono <select> nel subcategoryContainer
        const selectsWrappers = [...subcategoryContainer.querySelectorAll(".mb-3")];
        const index = selectsWrappers.findIndex(div => div.contains(currentSelect));

        // Se index >= 0, rimuovi tutti i wrapper successivi
        if (index >= 0) {
            for (let i = selectsWrappers.length - 1; i > index; i--) {
                selectsWrappers[i].remove();
            }
        } else if (!currentSelect) {
            // Se currentSelect è null, vuol dire che stiamo resettando tutto
            subcategoryContainer.innerHTML = "";
        }
    }

    // ------ PARTE INIZIALE: caricamento delle categorie "padre" (quelle top-level) ------
    // Inizialmente, abbiamo già un <select> di "categorie principali" in pagina (id="category-select").
    const categorySelect = document.getElementById("category-select");

    categorySelect.addEventListener("change", function() {
        // Reset di tutto
        subcategoryContainer.innerHTML = "";
        characteristicsContainer.innerHTML = "";
        characteristicsGroup.style.display = "none";
        finalCategoryInput.value = "";

        const selectedCategoryId = this.value;
        if (selectedCategoryId) {
            // Carico le *sotto* categorie relative a categorySelect
            // Prima di tutto bisogna vedere se la "categoria principale" stessa può avere un figlio
            loadSubcategories(selectedCategoryId, null);
        } else {
            // Nessuna categoria selezionata
        }
        checkFormValidity();
    });

    // Controllo sulla descrizione per attivare il tasto submit
    document.getElementById("description").addEventListener("input", checkFormValidity);

    // Disabilitiamo inizialmente il tasto di invio
    document.getElementById("submitButton").disabled = true;
});

