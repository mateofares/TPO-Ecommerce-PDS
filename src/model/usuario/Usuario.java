package model.usuario;

public abstract class Usuario {
    protected long id;
    protected String nombre;
    protected String apellido;
    protected String email;
    protected String contrasenia;

    public Usuario(long id, String nombre, String apellido, String email, String contrasenia) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contrasenia = contrasenia;
    }

    public boolean login(String email, String contrasenia) {
        return this.email.equals(email) && this.contrasenia.equals(contrasenia);
    }

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public String getContrasenia() { return contrasenia; }
}
