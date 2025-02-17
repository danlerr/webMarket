document.addEventListener("DOMContentLoaded", function() {
    /**
     * Input di ricerca per filtrare le proposte.
     * @type {HTMLInputElement}
     */
    const searchInput = document.getElementById('search');
    
    /**
     * Select per filtrare le proposte in base allo stato.
     * @type {HTMLSelectElement}
     */
    const filterSelect = document.getElementById('status');
    
    /**
     * NodeList di elementi con la classe 'proposta-container'.
     * @type {NodeList}
     */
    const proposals = document.querySelectorAll('.proposta-container');
  
    /**
     * Filtra le proposte in base al termine di ricerca e allo stato selezionato.
     */
    function filterProposte() {
      // prendo valore della ricerca (in minuscolo)
      const searchTerm = searchInput.value.toLowerCase();
      // prendo il valore selezionato per lo stato
      const selectedStatus = filterSelect.value;
      
      proposals.forEach(function(proposal) {
        // prendo il codice (in minuscolo) e lo stato della proposta dagli attributi dati
        const codice = proposal.getAttribute('data-codice').toLowerCase();
        const stato = proposal.getAttribute('data-stato');
  
        // Controllo se il codice include il termine di ricerca
        const matchCodice = codice.includes(searchTerm);
        // Controllo se lo stato corrisponde oppure se il filtro è impostato su "tutti"
        const matchStato = (selectedStatus === 'tutti' || stato === selectedStatus);
  
        // Se entrambi i controlli sono veri, mostro la proposta, altrimenti la nascondo
        if (matchCodice && matchStato) {
          proposal.style.display = '';
        } else {
          proposal.style.display = 'none';
        }
      });
    }
    
    // Aggiungo il listener all'input per il filtraggio in tempo reale
    searchInput.addEventListener('input', filterProposte);
    
    // Aggiungo il listener anche al select per il filtro per stato
    filterSelect.addEventListener('change', filterProposte);
  });
  