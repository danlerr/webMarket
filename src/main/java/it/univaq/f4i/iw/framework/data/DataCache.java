package it.univaq.f4i.iw.framework.data;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Didattica
 */
/**
 * Classe che implementa un sistema di caching per oggetti di tipo DataItem.
 * Questa cache permette di ridurre il numero di accessi al database,
 * migliorando le prestazioni dell'applicazione.
 * 
 * La cache utilizza una mappa nidificata:
 * - La chiave principale è la classe dell'oggetto.
 * - Il valore è un'altra mappa che associa la chiave dell'oggetto alla sua istanza.
 * 
 * Funzionalità principali:
 * - Aggiunta di oggetti alla cache.
 * - Recupero rapido degli oggetti memorizzati.
 * - Verifica della presenza di un oggetto nella cache.
 * - Rimozione di oggetti dalla cache quando non più necessari.
 */
public class DataCache {

    public Map<Class, Map<Object, Object>> cache;

    public DataCache() {
        this.cache = new HashMap<>();
    }

    // Aggiunge un oggetto alla cache.
    public <C extends DataItem> void add(Class<C> c, C o) {
        //Logger.getLogger("DataCache").log(Level.INFO, "Cache add: object of class {0} with key {1}", new Object[]{c.getName(), o.getKey()});
        if (!cache.containsKey(c)) {
            cache.put(c, new HashMap<>());
        }
        cache.get(c).put(o.getKey(), o);
    }

    //Rimuove un oggetto dalla cache, se presente.
    public <C extends DataItem> void delete(Class<C> c, C o) {
        if (has(c, o.getKey())) {
            cache.get(c).remove(o.getKey());
        }
    }

    //Verifica se un oggetto è presente nella cache.
    public <C extends DataItem> boolean has(Class<C> c, C o) {
        //Logger.getLogger("DataCache").log(Level.INFO, "Cache lookup: object of class {0} with key {1}", new Object[]{c.getName(), o.getKey()});
        return cache.containsKey(c) && cache.get(c).containsKey(o.getKey());
    }

    //Recupera un oggetto dalla cache.
    public <C extends DataItem> C get(Class<C> c, Object key) {
        if (has(c, key)) {
            //Logger.getLogger("DataCache").log(Level.INFO, "Cache hit: object of class {0} with key {1}", new Object[]{c.getName(), key});
            return (C) cache.get(c).get(key);
        } else {
            return null;
        }
    }

    //Metodo alternativo per verificare se un oggetto con una specifica chiave è nella cache.
    public boolean has(Class c, Object key) {
        //Logger.getLogger("DataCache").log(Level.INFO, "Cache lookup: object of class {0} with key {1}", new Object[]{c.getName(), key});
        return cache.containsKey(c) && cache.get(c).containsKey(key);
    }

    //Rimuove un oggetto dalla cache utilizzando la chiave univoca.
    public void delete(Class c, Object key) {
        if (has(c, key)) {
            cache.get(c).remove(key);
        }
    }

}
