-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Creato il: Feb 17, 2025 alle 02:12
-- Versione del server: 10.4.28-MariaDB
-- Versione PHP: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `webdb2`
--

DELIMITER $$
--
-- Funzioni
--
CREATE DEFINER=`root`@`localhost` FUNCTION `generate_codice` () RETURNS CHAR(10) CHARSET utf8mb4 COLLATE utf8mb4_general_ci DETERMINISTIC BEGIN
    DECLARE chars CHAR(62) DEFAULT 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    DECLARE result CHAR(10) DEFAULT '';
    DECLARE i INT DEFAULT 0;

    WHILE i < 10 DO
        SET result = CONCAT(result, SUBSTRING(chars, FLOOR(1 + RAND() * 62), 1));
        SET i = i + 1;
    END WHILE;

    RETURN result;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Struttura della tabella `caratteristica`
--

CREATE TABLE `caratteristica` (
  `id` int(11) NOT NULL,
  `nome` varchar(255) NOT NULL,
  `version` bigint(20) UNSIGNED NOT NULL DEFAULT 1,
  `categoria_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `caratteristica`
--

INSERT INTO `caratteristica` (`id`, `nome`, `version`, `categoria_id`) VALUES
(9, 'Genere', 1, 11),
(10, 'Numero di pagine ', 1, 11),
(11, 'RAM', 1, 24),
(12, 'Spazio di archiviazione', 1, 24),
(13, 'Pollici display', 1, 25),
(14, 'RAM', 1, 25),
(15, 'Spazio di archiviazione', 1, 25),
(16, 'Tipo di tasti', 1, 29),
(17, 'Wirless', 1, 29),
(18, 'Wirless', 1, 30),
(19, 'DPI desiderati', 1, 30),
(20, 'Dimensione', 1, 34),
(21, 'Marca', 1, 34),
(22, 'Dimensione', 1, 33),
(23, 'Memoria', 1, 33),
(24, 'Megapixel', 1, 17),
(25, 'Capacità', 1, 36),
(26, 'Tipo di porta', 1, 36),
(27, 'Tipo di apertura', 1, 35),
(28, 'Classe energetica', 1, 35),
(29, 'Capacità di carico', 1, 37),
(30, 'Con asciugatrice', 1, 37),
(31, 'Materiale', 1, 21),
(32, 'Dimensione', 1, 21),
(33, 'Colore', 1, 21),
(34, 'Tipologia illuminazione', 1, 22),
(35, 'Tipo di colore', 1, 22),
(36, 'Dimensione', 1, 23);

-- --------------------------------------------------------

--
-- Struttura della tabella `caratteristica_richiesta`
--

CREATE TABLE `caratteristica_richiesta` (
  `id` int(11) NOT NULL,
  `richiesta` int(11) NOT NULL,
  `caratteristica` int(11) NOT NULL,
  `valore` varchar(200) NOT NULL DEFAULT 'undefined',
  `version` bigint(20) UNSIGNED NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `caratteristica_richiesta`
--

INSERT INTO `caratteristica_richiesta` (`id`, `richiesta`, `caratteristica`, `valore`, `version`) VALUES
(20, 15, 24, '45', 1),
(21, 16, 31, 'Legno', 1),
(22, 17, 9, 'Horror', 1),
(23, 17, 10, '300/400', 1),
(24, 18, 36, '20x60x24', 1),
(25, 19, 30, 'Si', 1),
(26, 20, 23, '128 GB', 1),
(27, 21, 11, '64', 1),
(28, 21, 12, '1 TB', 1);

-- --------------------------------------------------------

--
-- Struttura della tabella `categoria`
--

CREATE TABLE `categoria` (
  `id` int(11) NOT NULL,
  `nome` varchar(255) NOT NULL,
  `version` bigint(20) UNSIGNED NOT NULL DEFAULT 1,
  `padre` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `categoria`
--

INSERT INTO `categoria` (`id`, `nome`, `version`, `padre`) VALUES
(1, 'Elettronica', 1, NULL),
(9, 'Elettrodomestici', 1, NULL),
(10, 'Arredamento & design', 1, NULL),
(11, 'Libri', 1, NULL),
(14, 'Computer e accessori', 1, 1),
(15, 'Telefonia ', 1, 1),
(17, 'Fotografia ', 1, 1),
(18, 'Cucina', 1, 9),
(19, 'Lavanderia', 1, 9),
(21, 'Mobili', 1, 10),
(22, 'Illuminazione', 1, 10),
(23, 'Elementi decorativi', 1, 10),
(24, 'Pc desktop', 1, 14),
(25, 'Pc laptop', 1, 14),
(27, 'Accessori per pc', 1, 14),
(28, 'Monitor', 1, 27),
(29, 'Tastiera', 1, 27),
(30, 'Mouse', 1, 27),
(33, 'Tablet', 1, 15),
(34, 'Smartphone', 1, 15),
(35, 'Frigoriferi', 1, 18),
(36, 'Lavastoviglie', 1, 18),
(37, 'Lavatrice', 1, 19);

-- --------------------------------------------------------

--
-- Struttura della tabella `ordine`
--

CREATE TABLE `ordine` (
  `id` int(11) NOT NULL,
  `data` date DEFAULT NULL,
  `stato` enum('IN_ATTESA','ACCETTATO','RESPINTO_NON_CONFORME','RESPINTO_NON_FUNZIONANTE','RIFIUTATO') NOT NULL,
  `version` bigint(20) UNSIGNED NOT NULL DEFAULT 1,
  `proposta_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `ordine`
--

INSERT INTO `ordine` (`id`, `data`, `stato`, `version`, `proposta_id`) VALUES
(10, '2025-02-16', 'ACCETTATO', 2, 16),
(11, '2025-02-16', 'ACCETTATO', 2, 18),
(12, '2025-02-17', 'RESPINTO_NON_CONFORME', 2, 19),
(13, '2025-02-17', 'ACCETTATO', 2, 21);

-- --------------------------------------------------------

--
-- Struttura della tabella `proposta`
--

CREATE TABLE `proposta` (
  `id` int(11) NOT NULL,
  `produttore` varchar(500) NOT NULL,
  `prodotto` varchar(500) NOT NULL,
  `codice` varchar(500) NOT NULL,
  `codice_prodotto` varchar(50) NOT NULL,
  `prezzo` float NOT NULL,
  `URL` text NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `stato` enum('ACCETTATO','RIFIUTATO','IN_ATTESA','ORDINATO') NOT NULL,
  `data` date DEFAULT NULL,
  `motivazione` text DEFAULT NULL,
  `version` bigint(20) UNSIGNED NOT NULL DEFAULT 1,
  `richiesta_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `proposta`
--

INSERT INTO `proposta` (`id`, `produttore`, `prodotto`, `codice`, `codice_prodotto`, `prezzo`, `URL`, `note`, `stato`, `data`, `motivazione`, `version`, `richiesta_id`) VALUES
(16, 'Canon', 'Canon EOS R5 II', 'RDcIR9IrFT', '0261230029', 6289, 'https://sabatinifotografia.it/s/canon-eos-r5-ii-24-105-f4/?tpid=T1lDRlhvdVkrT09RSEY4U2sxaS9ieVk1UWNoNnRhUWZvNnBPRDYvWkNQZkdGeHN5Y1IyNlptblNkRzNkTXdMUQ2', 'Con questa potrai fotografare professionalmente quello che vuoi, a qualsiasi dettaglio!', 'ORDINATO', '2025-02-16', NULL, 3, 15),
(17, 'Maisons du Monde', 'Avignon - Vetrina turchese in mango effetto anticato', 'rPH16HvdFC', '121425', 749, 'https://www.maisonsdumonde.com/IT/it/p/vetrina-turchese-in-mango-effetto-anticato-l-105-cm-avignon-121425.htm?utm_source=google&utm_medium=cpc&utm_campaign=SEA-GOO-B2C-INT-IT-IT-DCD-MDM-GEN-PMAX-HIGH&gad_source=1&gclid=CjwKCAiAtsa9BhAKEiwAUZAszXB8tv-QKJP1revHttmU0gAbiSfBwKADGoonUDsIjzsNbWzhr8JNeBoC9_YQAvD_BwE', NULL, 'RIFIUTATO', '2025-02-16', 'Grazie per la proposta, ma vorrei un mobile più basso\n', 2, 16),
(18, 'UAIN', 'Credenza Uain in legno di mango', 'XkxNprhhHA', '138658-291318', 459.95, 'https://www.sklum.com/it/acquistare-credenza/138658-credenza-uain-in-legno-di-mango.html?id_c=291318&gad_source=1&gclid=CjwKCAiAtsa9BhAKEiwAUZAszZxcIfGbnI9J4gDx4fLei8SZVd74vRp2CYfvyNhQnSi4-b7el5t_qhoCX4IQAvD_BwE', NULL, 'ORDINATO', '2025-02-16', NULL, 3, 16),
(19, 'Jason Rekulak', 'Teddy', '2yarL0q0Nf', '9788809959699', 16.05, 'https://www.mondadoristore.it/Teddy-Jason-Rekulak/eai978880995969/', 'Ultimamente parlano bene di questo', 'ORDINATO', '2025-02-17', NULL, 3, 17),
(20, 'Arttor', 'Quadro eclissi totale', 'XLxb01xXhj', 'au5l2md', 54.99, 'https://arttor.it/product-ita-415128-Quadro-su-tela-Stampe-su-Tela-Eclissi-totale-120x80-cm.html?gad_source=1&gclid=CjwKCAiAtsa9BhAKEiwAUZAszbLrXmPfwB6RUdablunpOVcGQxeK0Ifd1eRHPB-qYQfHQCvS_r3S6hoCMqkQAvD_BwE', 'Ho trovato solo questa dimensione', 'RIFIUTATO', '2025-02-17', 'Troppo grande', 2, 18),
(21, 'HISENSE', 'LAVASCIUGA HISENSE WDQA1014EVJM', 'yF9pWy9ee9', '186815', 439.99, 'https://www.mediaworld.it/it/product/_hisense-wdqa1014evjm-186815.html?utm_source=google&utm_medium=cpc&utm_campaign=rt_shopping_na_sp_na_ged&utm_term=&utm_content=186815&gad_source=1&gclid=CjwKCAiAtsa9BhAKEiwAUZAszVopIpPXX89XnJXYHDBsdac5S2LuxyrMmG0qYR5GROqD5VCBa1JnwhoCivYQAvD_BwE&gclsrc=aw.ds', 'Con questa vai sul sicuro', 'ORDINATO', '2025-02-17', NULL, 3, 19);

--
-- Trigger `proposta`
--
DELIMITER $$
CREATE TRIGGER `before_insert_proposta` BEFORE INSERT ON `proposta` FOR EACH ROW BEGIN
    IF NEW.codice IS NULL OR NEW.codice = '' THEN
        SET NEW.codice = generate_codice();
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Struttura della tabella `recensione`
--

CREATE TABLE `recensione` (
  `id` int(11) NOT NULL,
  `valore` int(11) NOT NULL,
  `autore` int(11) NOT NULL,
  `destinatario` int(11) NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `recensione`
--

INSERT INTO `recensione` (`id`, `valore`, `autore`, `destinatario`, `version`) VALUES
(8, 5, 19, 22, 1),
(9, 4, 18, 23, 1),
(10, 2, 25, 24, 1),
(11, 5, 25, 20, 1);

-- --------------------------------------------------------

--
-- Struttura della tabella `richiesta`
--

CREATE TABLE `richiesta` (
  `id` int(11) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `stato` enum('IN_ATTESA','PRESA_IN_CARICO','RISOLTA','ORDINATA') NOT NULL,
  `data` date DEFAULT NULL,
  `codice_richiesta` varchar(255) NOT NULL,
  `version` bigint(20) UNSIGNED NOT NULL DEFAULT 1,
  `ordinante` int(11) NOT NULL,
  `tecnico` int(11) DEFAULT NULL,
  `categoria` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `richiesta`
--

INSERT INTO `richiesta` (`id`, `note`, `stato`, `data`, `codice_richiesta`, `version`, `ordinante`, `tecnico`, `categoria`) VALUES
(15, 'Vorrei una fotocamera professionale, sono disposto a spendere molto', 'RISOLTA', '2025-02-16', 'FB2RyLd12w', 4, 19, 22, 17),
(16, 'Mi piacerebbe uno stile antico', 'RISOLTA', '2025-02-16', 'Vx5PbcBqWu', 4, 18, 23, 21),
(17, 'Non voglio un solito horror noioso, trovami per favore qualcosa di nuovo!', 'RISOLTA', '2025-02-17', 'preUOH4NXM', 4, 25, 24, 11),
(18, 'Vorrei un quadro gotico ', 'PRESA_IN_CARICO', '2025-02-17', 'LS4qoPRnW3', 2, 26, 22, 23),
(19, 'Vorrei una lavatrice a  basso consumo possibilmente', 'RISOLTA', '2025-02-17', 'Oac8izc00p', 4, 25, 20, 37),
(20, 'Lo vorrei bello grande per studiare e leggere', 'PRESA_IN_CARICO', '2025-02-17', 'dYku6fr7tx', 2, 26, 20, 33),
(21, '', 'IN_ATTESA', '2025-02-17', 'Ls9zMf5FrO', 1, 19, NULL, 24);

--
-- Trigger `richiesta`
--
DELIMITER $$
CREATE TRIGGER `before_insert_richiesta` BEFORE INSERT ON `richiesta` FOR EACH ROW BEGIN
    IF NEW.codice_richiesta IS NULL OR NEW.codice_richiesta = '' THEN
        SET NEW.codice_richiesta = generate_codice();
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Struttura della tabella `utente`
--

CREATE TABLE `utente` (
  `id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `tipologia_utente` enum('ORDINANTE','TECNICO','AMMINISTRATORE','') NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `utente`
--

INSERT INTO `utente` (`id`, `username`, `email`, `password`, `tipologia_utente`, `version`) VALUES
(1, 'admin', 'admin@admin.com', '282db4a4425f50237e7df29d56988825f15dd8b34fa74af54e650ce0fd8897a82dff0b952017a3a88a62f5f1b0e0e467', 'AMMINISTRATORE', 1),
(7, 'ordinante', 'ord@ord.com', '322338044467593cacc3375e479cd465e89802c197d22d2a7342eb1dbb3ece214fffe1b1621bbc1f89fe33fcfc399ec5', 'ORDINANTE', 1),
(8, 'tecnico', 'tec@tec.com', 'd2f5b962a55f32a640a8b7b9132e7046bc4ffbb77078a17db7c7d4e2d8e75bbe4790b25005cb523c918901d8ff5a60ab', 'TECNICO', 1),
(17, 'jampod', 'jampodpodcast@gmail.com', '81b5eeb638448f8e03d8d159cbd4c8df217a02687ac3e13d6627df64a3a87550a78a9ad60f851f4a7975ab18a1021315', 'ORDINANTE', 1),
(18, 'daniele', 'daniele@gmail.com', '31686422f0cb4f9ecdce66e9a1233af354028e59890cd99b163938ac96d9197d40f1a4a989ce22d0cd42be9d4249f334', 'ORDINANTE', 1),
(19, 'leonardo', 'leonardo@gmail.com', 'c0988e942d684b3e76f9407f1e74f86d842cce4e3a721219adbe27098c8f361d9cf3dc997e3c330c7b17cee0ffbd160d', 'ORDINANTE', 1),
(20, 'elisabetta', 'elisabetta@gmail.com', '07aa4f22552ed6a13a391cbece8811f20c1843884ae71bbef7c89151f0dca5fba1295b26f0bb583158bf2b83498d328c', 'TECNICO', 1),
(22, 'giuseppe', 'giuseppe@gmail.com', '4128753489550e84225cc313284d1c85d5f4e95803a021f93ea103c5083bf78b4000ede92295072c39acd567fb8fd40a', 'TECNICO', 1),
(23, 'tecman', 'tecman@gmail.com', '6c376a6ffce3b12fa9bcd53a02fd5c610d1ccb19d72e7fde117af2fd422f068f84e6c017e796342656be31a6004a9182', 'TECNICO', 1),
(24, 'trovatutto', 'trovatutto@gmail.com', '167552b1ee784f54fc6d9cae50e747ccac12eb844f1a98e50269ef6dff56fa1fcd784a8796f3f504c44748f8eb9167e1', 'TECNICO', 1),
(25, 'antonio', 'antonio@gmail.com', 'cf64597e7469683ebcd98997ed518b50b77b52d3203486badcc053c1d4ff063d4465873e6638d6bd296f2959096e79a5', 'ORDINANTE', 1),
(26, 'giordano', 'giordano@gmail.com', 'a5086617513c3538479e23b79a7f096dd862dd0fa9ae4757f31a252702db32d65d99ce11d335d254106460f60c294f4a', 'ORDINANTE', 1);

--
-- Indici per le tabelle scaricate
--

--
-- Indici per le tabelle `caratteristica`
--
ALTER TABLE `caratteristica`
  ADD PRIMARY KEY (`id`),
  ADD KEY `caratteristica_ibfk_1` (`categoria_id`);

--
-- Indici per le tabelle `caratteristica_richiesta`
--
ALTER TABLE `caratteristica_richiesta`
  ADD PRIMARY KEY (`id`),
  ADD KEY `richiesta` (`richiesta`,`caratteristica`),
  ADD KEY `caratteristica` (`caratteristica`);

--
-- Indici per le tabelle `categoria`
--
ALTER TABLE `categoria`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nome` (`nome`);

--
-- Indici per le tabelle `ordine`
--
ALTER TABLE `ordine`
  ADD PRIMARY KEY (`id`),
  ADD KEY `proposta_id` (`proposta_id`);

--
-- Indici per le tabelle `proposta`
--
ALTER TABLE `proposta`
  ADD PRIMARY KEY (`id`),
  ADD KEY `richiesta_id` (`richiesta_id`);

--
-- Indici per le tabelle `recensione`
--
ALTER TABLE `recensione`
  ADD PRIMARY KEY (`id`),
  ADD KEY `autore` (`autore`,`destinatario`),
  ADD KEY `destinatario` (`destinatario`);

--
-- Indici per le tabelle `richiesta`
--
ALTER TABLE `richiesta`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `codice_richiesta` (`codice_richiesta`),
  ADD KEY `ordinante` (`ordinante`,`tecnico`,`categoria`),
  ADD KEY `categoria` (`categoria`),
  ADD KEY `tecnico` (`tecnico`);

--
-- Indici per le tabelle `utente`
--
ALTER TABLE `utente`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT per le tabelle scaricate
--

--
-- AUTO_INCREMENT per la tabella `caratteristica`
--
ALTER TABLE `caratteristica`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- AUTO_INCREMENT per la tabella `caratteristica_richiesta`
--
ALTER TABLE `caratteristica_richiesta`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT per la tabella `categoria`
--
ALTER TABLE `categoria`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=39;

--
-- AUTO_INCREMENT per la tabella `ordine`
--
ALTER TABLE `ordine`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT per la tabella `proposta`
--
ALTER TABLE `proposta`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT per la tabella `recensione`
--
ALTER TABLE `recensione`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT per la tabella `richiesta`
--
ALTER TABLE `richiesta`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT per la tabella `utente`
--
ALTER TABLE `utente`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- Limiti per le tabelle scaricate
--

--
-- Limiti per la tabella `caratteristica`
--
ALTER TABLE `caratteristica`
  ADD CONSTRAINT `caratteristica_ibfk_1` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limiti per la tabella `caratteristica_richiesta`
--
ALTER TABLE `caratteristica_richiesta`
  ADD CONSTRAINT `caratteristica_richiesta_ibfk_1` FOREIGN KEY (`richiesta`) REFERENCES `richiesta` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `caratteristica_richiesta_ibfk_2` FOREIGN KEY (`caratteristica`) REFERENCES `caratteristica` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limiti per la tabella `ordine`
--
ALTER TABLE `ordine`
  ADD CONSTRAINT `ordine_ibfk_1` FOREIGN KEY (`proposta_id`) REFERENCES `proposta` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limiti per la tabella `proposta`
--
ALTER TABLE `proposta`
  ADD CONSTRAINT `proposta_ibfk_1` FOREIGN KEY (`richiesta_id`) REFERENCES `richiesta` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limiti per la tabella `recensione`
--
ALTER TABLE `recensione`
  ADD CONSTRAINT `recensione_ibfk_1` FOREIGN KEY (`autore`) REFERENCES `utente` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `recensione_ibfk_2` FOREIGN KEY (`destinatario`) REFERENCES `utente` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limiti per la tabella `richiesta`
--
ALTER TABLE `richiesta`
  ADD CONSTRAINT `richiesta_ibfk_1` FOREIGN KEY (`categoria`) REFERENCES `categoria` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `richiesta_ibfk_2` FOREIGN KEY (`ordinante`) REFERENCES `utente` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `richiesta_ibfk_3` FOREIGN KEY (`tecnico`) REFERENCES `utente` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
