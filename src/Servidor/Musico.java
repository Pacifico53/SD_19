package Servidor;

public class Musico extends Utilizador{
    private String nome;
    
    public Musico(String username, String password, String nome){
        super(username, password);
        this.nome = nome;
    }

    public String getNome(){
        return this.nome;
    }
}
