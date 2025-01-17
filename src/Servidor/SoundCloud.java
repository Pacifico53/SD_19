package Servidor;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SoundCloud {
    private HashMap<String, Utilizador> users;
    private HashMap<Integer, Ficheiro> musicas;
    private HashMap<String, ServerMessage> user_messages;
    private ReentrantLock lockSC;
    private ReentrantLock lockUsers;
    private ReentrantLock lockMsgs;

    public SoundCloud() {
        this.users = new HashMap<>();
        this.musicas = new HashMap<>();
        this.user_messages = new HashMap<>();
        this.lockSC = new ReentrantLock();
        this.lockUsers = new ReentrantLock();
        this.lockMsgs = new ReentrantLock();
    }

    public void createUser(String username, String pass, ServerMessage sm) throws UsernameTakenException {
        this.lockUsers.lock();
        try{
            if(this.users.containsKey(username)){
                throw new UsernameTakenException("Username already taken.");
            }
            else {
                Utilizador u = new Utilizador(username ,pass);
                this.users.put(username,u);
                this.lockMsgs.lock();
                try{
                    this.user_messages.put(username, sm);
                }
                finally{this.lockMsgs.unlock();}
            }
        }
        finally{
            this.lockUsers.unlock();
        }
    }

    public Utilizador login(String username, String password, ServerMessage sm) throws UsernameInexistenteException, PasswordIncorretaException {
        this.lockUsers.lock();

        try{
            if(!this.users.containsKey(username)){
                throw new UsernameInexistenteException("Invalid username.");
            }
            else if(!this.users.get(username).getPassword().equals(password)){
                    throw new PasswordIncorretaException("Invalid password.");
            }
        }
        finally{
            this.lockUsers.unlock();
        }

        this.lockMsgs.lock();
        try{
            if(this.user_messages.containsKey(username)){
                ServerMessage m = this.user_messages.get(username);
                
                String linha;
                while((linha = m.getMessage()) != null){
                    sm.setMessage(linha, null);
                }
                this.user_messages.put(username, sm);
            }
        }
        finally{
            this.lockMsgs.unlock();
        }

        this.lockUsers.lock();

        try{
            return this.users.get(username);
        }
        finally{
            this.lockUsers.unlock();
        }
    }

    // Adicionar música
    public Ficheiro upload(Ficheiro f, Socket socket, int filesize) {
        this.lockSC.lock();
        
        try {
            int id = this.musicas.size();
            f.setId(id);
            try {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                FileOutputStream fos;
    
                fos = new FileOutputStream("../MusicFiles/" + f.getId() + "_" + f.getNome() + ".mp3");
                byte[] buffer = new byte[1048];
        
                int read = 0;
                int remaining = filesize;
                while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                    remaining -= read;
                    fos.write(buffer, 0, read);
                }
                
                fos.flush();
                fos.close();       
            } catch (Exception e) {
                e.printStackTrace();
            }            
            this.musicas.put(id, f);

            return f;
        }
        finally{
            this.lockSC.unlock();
        }
    }

    //Descarregar música
    public Ficheiro download(int id){
        this.lockSC.lock();

        try{
            Ficheiro f = null;
            f = this.musicas.get(id);
            f.incTimesPlayed();
            this.musicas.put(id, f);
            return f;
        }
        finally{
            this.lockSC.lock();
        }
    }

    //Pesquisar música
    public ArrayList<Ficheiro> search(String label){
        this.lockSC.lock();
        try{
            String[] separated_labels = label.split(" ");

            ArrayList<Ficheiro> lista = new ArrayList<Ficheiro>();
            for (Ficheiro f : this.musicas.values()) {
                for (String l : separated_labels) {
                    if (f.getLabels().contains(l))
                        lista.add(f);
                }
            }
            return lista;
        }
        finally{
            this.lockSC.unlock();
        }
    }
}

