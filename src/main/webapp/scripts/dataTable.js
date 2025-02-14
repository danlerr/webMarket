document.addEventListener("DOMContentLoaded", function () {
  // Formatto la colonna "Stato" per ordinare con priorità definita
  document.querySelectorAll(".datatable tbody tr").forEach(function (row) {
      var tdStato = row.cells[5]; // Colonna Stato
      if (tdStato) {
          var statoTesto = tdStato.textContent.trim();

          // Mappa degli stati con priorità numerica
          var statoPriorita = {
              "Ordinata": 1,
              "Risolta": 2,
              "Presa in carico": 3,
              "In attesa": 4
          };

          // Imposta l'attributo data-order in base alla priorità
          tdStato.setAttribute("data-order", statoPriorita[statoTesto] || 99);
      }
  });

  // Inizializza DataTables
  var table = new DataTable('.datatable', {
      paging: false,        // Disabilita la paginazione
      info: false,          // Nasconde il messaggio "Pagina X di Y"
      lengthChange: false,  // Rimuove il selettore del numero di righe
      order: [[4, "desc"]], // Ordina per data in ordine decrescente
      columnDefs: [
          { orderable: true, targets: 5 } // Permette di ordinare la colonna Stato con il data-order
      ],
      language: {
          zeroRecords: "Nessuna richiesta trovata",
          search: "Cerca:"
      }
  });
});