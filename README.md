# Progetto per l'esame di Progetto di Sistemi Web - Backend ReST
## Introduzione
Questa è la parte backend del mio progetto, e si occupa di fornire un servizio ReST.\
Il servizio permette ai client di effettuare operazioni CRUD su una base dati di canzoni.
## Metodo per l'integrazione dei sistemi
Il backend fornisce output JSON, sia per le operazioni di GET che per le altre operazioni (in quest'ultimo
caso, il JSON conterrà informazioni sull'esito della richiesta). Il frontend consumerà il servizio ReST
tramite chiamate HTTP e permetterà la visualizzazione e la modifica dei dati in maniera intuitiva.
### Formato del JSON
Il formato del JSON scelto per rappresentare una risorsa di tipo "Music" è il seguente, e rispecchia
il formato dei record nella base dati:

    {
        "id"    : number,
        "title" : string,
        "author": string,
        "album" : string,
        "year"  : string,
        "genre" : string,
        "url"   : string
    }
    
Per quanto riguarda il JSON fornito come risposta, si definito il seguente formato:

    {
        "httpStatus"    : number,
        "requestStatus" : "success"/"error",
        "message"   : string,
        "data"      : [{Music1}, {Music2}, ...]
    }

dove "message" è un messaggio o una descrizione dell'errore in caso si verificasse.\
"data" è un Array di oggetti JSON di tipo Music.
## Database
Per un primo esempio di base dati si è utilizzato SQLite, un database molto leggero. In un caso d'uso
reale, è facile modificare il codice per supportare altri tipi di database relazionali,
quali MySQL o PostgreSQL. La connessione alla base dati viene gestita con JDBC.