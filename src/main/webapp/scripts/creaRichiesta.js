document.addEventListener("DOMContentLoaded", function() {
  const categorySelect = document.getElementById("category-select");
  const subcategoryGroup = document.getElementById("subcategory-group");
  const subcategorySelect = document.getElementById("subcategory-select");
  const characteristicsGroup = document.getElementById("characteristics-group");
  const characteristicsContainer = document.getElementById("characteristics-container");
  const submitButton = document.getElementById("submitButton");
  const ajaxErrorMessageDiv = document.getElementById("ajax-error-message"); // Aggiunto

  // Funzione per abilitare/disabilitare il pulsante submit
  function checkFormValidity() {
      const subcategoryValue = subcategorySelect.value;
      const descriptionValue = document.getElementById("description").value.trim();
      if (subcategoryValue !== "" && descriptionValue !== "") {
          submitButton.disabled = false;
      } else {
          submitButton.disabled = true;
      }
  }

  // Gestione del cambio della categoria principale
  categorySelect.addEventListener("change", function() {
      const selectedCategoryId = this.value;
      subcategorySelect.innerHTML = '<option value="">Seleziona una sottocategoria</option>';
      characteristicsContainer.innerHTML = "";
      characteristicsGroup.style.display = "none";
      submitButton.disabled = true;
      ajaxErrorMessageDiv.style.display = "none"; // Nascondi messaggi di errore precedenti

      if (selectedCategoryId !== "") {
          // URL RELATIVO AL CONTESTO
          fetch("creaRichiesta?action=getSubcategories&parentCategoryId=" + selectedCategoryId)
              .then(response => {
                  if (!response.ok) { // Controlla lo stato HTTP
                     throw new Error("Errore di rete: " + response.status);
                  }
                  return response.json();
              })
              .then(data => {
                  if (data && data.length > 0) {
                      data.forEach(subcat => {
                          const option = document.createElement("option");
                          option.value = subcat.key;
                          option.textContent = subcat.nome;
                          subcategorySelect.appendChild(option);
                      });
                      subcategoryGroup.style.display = "block";
                  } else {
                      subcategoryGroup.style.display = "none";
                  }
              })
              .catch(error => {
                  console.error("Errore nel recupero delle sottocategorie:", error);
                  ajaxErrorMessageDiv.textContent = "Errore nel caricamento delle sottocategorie. Riprova.";
                  ajaxErrorMessageDiv.style.display = "block";
              });
      } else {
          subcategoryGroup.style.display = "none";
      }
  });

  // Gestione del cambio della sottocategoria
  subcategorySelect.addEventListener("change", function() {
      const selectedSubcategoryId = this.value;
      characteristicsContainer.innerHTML = "";
      characteristicsGroup.style.display = "none";
      submitButton.disabled = true;
      ajaxErrorMessageDiv.style.display = "none";

      if (selectedSubcategoryId !== "") {
          fetch("creaRichiesta?action=getCaratteristiche&subcategoryId=" + selectedSubcategoryId)
          .then(response => {
              if(!response.ok){
                throw new Error("Errore di rete: " + response.status);
              }
            return response.json();
          })
              .then(data => {
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
              })
              .catch(error => {
                  console.error("Errore nel recupero delle caratteristiche:", error);
                  ajaxErrorMessageDiv.textContent = "Errore nel caricamento delle caratteristiche. Riprova.";
                  ajaxErrorMessageDiv.style.display = "block";
              });
      }
      checkFormValidity();
  });

    // Verifica della validità del form in tempo reale (ad esempio per la descrizione)
    document.getElementById("description").addEventListener("input", function() {
    checkFormValidity();
  });

});