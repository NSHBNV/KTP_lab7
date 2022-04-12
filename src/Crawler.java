import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Pattern;

// Основной класс сканера
class Crawler {
    // Хэш-карта для хранения информации о ссылках
    private final HashMap<String, URLDepthPair> links = new HashMap<>();
    // Связанный список для хранения пар URL - глубина
    private final LinkedList<URLDepthPair> pool = new LinkedList<>();

    private int depth = 0;

    public Crawler(String url, int dep) {
        depth = dep;
        pool.add(new URLDepthPair(url, 0));
    }

    public void run() {
        while (pool.size() > 0)
            parseLink(pool.pop());

        // Выводим ссылки
        for (URLDepthPair link : links.values())
            System.out.println(link);

        System.out.println();
        System.out.printf("Found %d URLS\n", links.size());
    }

    // Преобразуем regular expression в паттерн
    public static Pattern LINK_REGEX = Pattern.compile(
            "<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1"
    );

    // Метод парсящий ссылку
    private void parseLink(URLDepthPair link) {
        // Если в links уже содержится такой URL, то создаем объект knownLink, присваиваем к нему ссылку на необходимый
        // нам объект и инкрементируем переменную visited
        if (links.containsKey(link.getURL())) {
            URLDepthPair knownLink = links.get(link.getURL());
            knownLink.incrementVisited();
            return;
        }
        // Иначе добавляем эту ссылку в хэш-карту
        links.put(link.getURL(), link);

        // Если глубина больше нам необходимой пропускаем итерацию
        if (link.getDepth() >= depth)
            return;

        try {
            // Создаем объект класса URL и передаем в конструктор URL-адрес
            URL url = new URL(link.getURL());
            // Вызываем для этого объекта метод openConnection и приводим его к типу HttpURLConnection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // Устанавливаем Http-метод GET
            con.setRequestMethod("GET");

            // Устанавливаем в качестве поток для Scanner con.getInputStream
            Scanner s = new Scanner(con.getInputStream());
            // Ищем по паттерну ссылки
            while (s.findWithinHorizon(LINK_REGEX, 0) != null) {
                // Ищем URL в группе номер 2 из regular expression
                String newURL = s.match().group(2);
                // Корректируем результат
                if (newURL.startsWith("/"))
                    newURL = link.getURL() + newURL;
                else if (!newURL.startsWith("http"))
                    continue;
                // Добавляем URL в список
                URLDepthPair newLink = new URLDepthPair(newURL, link.getDepth() + 1);
                pool.add(newLink);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void showHelp() {
        System.out.println("usage: java Crawler <URL> <depth>");
        System.exit(1);
    }
    public static void main(String[] args){
        Scanner scan=new Scanner(System.in);

        // Вводим данные с клавиатуры
        System.out.println("Enter URL: ");
        String url=scan.nextLine();

        System.out.println("Enter depth: ");
        int depth=0;
        try {
            depth=Integer.parseInt(scan.nextLine());
        } catch (Exception e) {
            showHelp();
        }

        Crawler crawler = new Crawler(url, depth);
        crawler.run();
    }
}