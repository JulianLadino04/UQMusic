package co.edu.uniquindio.Storify.utils;

import co.edu.uniquindio.Storify.model.*;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;

/**
 * Clase de utilidad para la lectura y escritura de archivos
 *
 * @author caflorezvi
 */
public class ArchivoUtils {

    /**
     * Permite leer un archivo desde una ruta específica mediante Scanner
     *
     * @param ruta Ruta a leer
     * @return Lista de String por cada línea del archivo
     * @throws IOException
     */
    public static ArrayList<String> leerArchivoScanner(String ruta) throws IOException {

        ArrayList<String> lista = new ArrayList<>();
        Scanner sc = new Scanner(new File(ruta));

        while (sc.hasNextLine()) {
            lista.add(sc.nextLine());
        }

        sc.close();

        return lista;
    }

    /**
     * Permite leer un archivo desde una ruta específica mediante BufferedReader
     *
     * @param ruta Ruta a leer
     * @return Lista de String por cada línea del archivo
     * @throws IOException
     */
    public static ArrayList<String> leerArchivoBufferedReader(String ruta) throws IOException {

        ArrayList<String> lista = new ArrayList<>();
        FileReader fr = new FileReader(ruta);
        BufferedReader br = new BufferedReader(fr);
        String linea;

        while ((linea = br.readLine()) != null) {
            lista.add(linea);
        }

        br.close();
        fr.close();

        return lista;
    }

    /**
     * Escribe datos en un archivo de texo
     *
     * @param ruta  Ruta donde se va a crear el archivo
     * @param lista Datos que se escriben en el archivo
     * @throws IOException
     */
    public static void escribirArchivoFormatter(String ruta, List<String> lista) throws IOException {
        Formatter ft = new Formatter(ruta);
        for (String s : lista) {
            ft.format(s + "%n");
        }
        ft.close();
    }

    public static void serializarUsuario(String ruta, HashMap<String, Usuario> clientes) {
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ruta))) {
            salida.writeObject(clientes);
            System.out.println("Cliente serializado correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void serializarArtista(String ruta, ArbolBinario arbolBinario) {
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ruta))) {
            salida.writeObject(arbolBinario);
            System.out.println("Artistas Serializados Correctamente");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void escribirArchivoBufferedWriter(String ruta, List<String> lista, boolean concat) throws IOException {

        FileWriter fw = new FileWriter(ruta, concat);
        BufferedWriter bw = new BufferedWriter(fw);

        for (String string : lista) {
            bw.write(string);
            bw.newLine();
        }
        bw.close();
        fw.close();
    }

    /**
     * Serializa un objeto en disco
     *
     * @param ruta   Ruta del archivo donde se va a serializar el objeto
     * @param objeto Objeto a serializar
     * @throws IOException
     */
    public static void serializarObjeto(String ruta, Object objeto) throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(ruta));
        os.writeObject(objeto);
        os.close();
    }

    /**
     * Deserializa un objeto que está guardado en disco
     *
     * @param ruta Ruta del archivo a deserializar
     * @return Objeto deserializado
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Object deserializarObjeto(String ruta) throws IOException, ClassNotFoundException {
        ObjectInputStream is = new ObjectInputStream(new FileInputStream(ruta));
        Object objeto = is.readObject();
        is.close();

        return objeto;
    }

    /**
     * Serializa un objeto en un archivo en formato XML
     *
     * @param ruta   Ruta del archivo donde se va a serializar el objeto
     * @param objeto Objeto a serializar
     * @throws FileNotFoundException
     */
    public static void serializarObjetoXML(String ruta, Object objeto) throws FileNotFoundException {
        XMLEncoder encoder = new XMLEncoder(new FileOutputStream(ruta));
        encoder.writeObject(objeto);
        encoder.close();
    }

    /**
     * Deserializa un objeto desde un archivo XML
     *
     * @param ruta Ruta del archivo a deserializar
     * @return Objeto deserializado
     * @throws IOException
     */
    public static Object deserializarObjetoXML(String ruta) throws IOException {
        XMLDecoder decoder = new XMLDecoder(new FileInputStream(ruta));
        Object objeto = decoder.readObject();
        decoder.close();

        return objeto;
    }

    public static List<Autor> cargarArtistas(String rutaArchivo) throws IOException {
        List<Autor> artistas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            Autor autorActual = null;

            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("#Artistas")) {
                    continue; // Saltar la línea de encabezado
                } else if (linea.startsWith("#Canciones")) {
                    break; // Fin de la sección de artistas
                } else {
                    String[] partes = linea.split(";");
                    if (partes.length == 4) {
                        String codigo = partes[0];
                        String nombre = partes[1];
                        String nacionalidad = partes[2];
                        boolean esGrupo = Boolean.parseBoolean(partes[3]);

                        autorActual = Autor.builder()
                                .codigo(codigo)
                                .nombre(nombre)
                                .nacionalidad(nacionalidad)
                                .esGrupo(esGrupo)
                                .listaCanciones(new ListaDoblementeEnlazada<>())
                                .build();

                        artistas.add(autorActual);
                    }
                }
            }
        }

        return artistas;
    }

    public static void cargarCanciones(String rutaArchivo, List<Autor> artistas) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;

            // Avanzar hasta la sección de canciones
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("#Canciones")) {
                    break;
                }
            }

            // Leer canciones
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(";");
                if (partes.length == 7) {
                    String nombreArtista = partes[0];
                    String nombreCancion = partes[1];
                    String nombreAlbum = partes[2];
                    int anio = Integer.parseInt(partes[3]);
                    double duracion = Double.parseDouble(partes[4]);
                    String genero = partes[5];
                    String url = partes[6];

                    // Buscar el autor correspondiente
                    Autor autor = buscarAutor(artistas, nombreArtista);
                    if (autor != null) {
                        Cancion cancion = Cancion.builder()
                                .nombreCancion(nombreCancion)
                                .nombreAlbum(nombreAlbum)
                                .anio(anio)
                                .duracion(duracion)
                                .genero(genero)
                                .url(url)
                                .build();

                        autor.getListaCanciones().añadirFinal(cancion);
                    }
                }
            }
        }
    }

    private static Autor buscarAutor(List<Autor> artistas, String nombre) {
        for (Autor autor : artistas) {
            if (autor.getNombre().equals(nombre)) {
                return autor;
            }
        }
        return null;
    }
}