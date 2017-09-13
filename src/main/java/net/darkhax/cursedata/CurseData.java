package net.darkhax.cursedata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;

public class CurseData {

    public static final Logger LOG = Logger.getLogger("CurseData");

    public static final String PROJECTS_PAGE_URL = "https://mods.curse.com/members/%s/projects?page=%d";

    public static final String WIDGET_URL = "https://widget.mcf.li%s.json";
    
    public static final String PROJECTS_URL = "https://minecraft.curseforge.com/projects";

    public static final Gson gson = new Gson();

    public static Project getProjectFromCurse (String curseId) {

        return getProjectFromWidget(String.format(WIDGET_URL, curseId));
    }

    public static Project getProjectFromWidget (String widgetUrl) {

        try {
    
            Project proj = getProjectFromJson(getDocument(widgetUrl).body().text());
            return proj != null && proj.getDownloads() == null ? null : proj;
        }

        catch (final IOException e) {

            LOG.info("Failed to read " + widgetUrl + " Retrying... " + e.getMessage());
        }

        return getProjectFromWidget(widgetUrl);
    }

    public static Project getProjectFromJson (String json) {

        return gson.fromJson(json, Project.class);
    }

    public static Member getMember (String curseForgeName) {

        int totalPages = 1;
        String username = "%INVALID%";
        String joined = "";
        String avatar = "";
        final List<Project> projects = new ArrayList<>();
        final boolean isDone = false;

        for (int page = 1; !isDone; page++) {
            try {

                final String docpage = String.format(PROJECTS_PAGE_URL, curseForgeName, page);
                final Document doc = getDocument(docpage);

                if (page == 1) {
                    for (final Element pageElement : doc.getElementsByClass("b-pagination-item")) {
                        final String text = pageElement.getElementsByClass("b-pagination-item").first().text();
                        if (Character.isDigit(text.charAt(0)))
                            if (text.length() <= String.valueOf(Integer.MAX_VALUE).length() && Integer.parseInt(text) > totalPages)
                                totalPages = Integer.parseInt(text);
                    }

                    username = doc.getElementsByClass("username").first().text();
                    joined = doc.getElementsByClass("joined").first().text();
                    avatar = doc.getElementsByClass("avatar-100").first().getElementsByTag("img").first().attr("src");
                }

                final Element e = doc.getElementsByClass("project-listing").first();

                for (final Element s : e.getElementsByTag("dt")) {

                    final String projectId = s.getElementsByTag("a").first().attr("href");

                    if (!projectId.startsWith("/mc-mods/minecraft/"))
                        continue;
                    final Project p = getProjectFromCurse(projectId);

                    if (p != null)
                        projects.add(p);
                }
            }

            catch (final IOException | NullPointerException exception) {

                if (exception instanceof HttpStatusException && ((HttpStatusException) exception).getStatusCode() == 404)
                    LOG.info(String.format("The cursename %s was invalid", curseForgeName));
                else
                    exception.printStackTrace();
                return new Member(username, avatar, joined, projects);
            }

            if (page == totalPages)
                break;
        }

        return new Member(username, avatar, joined, projects);
    }

    private static Document getDocument (String url) throws IOException {

        return Jsoup.connect(url).ignoreContentType(true).userAgent("Opera/9.80 (Macintosh; Intel Mac OS X 10.6.8; U; fr) Presto/2.9.168 Version/11.52").referrer("https://mods.curse.com").timeout(12000).ignoreHttpErrors(true).followRedirects(true).get();
    }
    
    public static long getTotalCurseDownloads(){
    
        try {
            final Document doc = getDocument(PROJECTS_URL);
            for(Element el : doc.getElementsByClass("project-category")) {
                for(Element ele : el.getElementsByClass("category-info")) {
                    if(ele.getElementsByTag("h2").first().text().contains("Mods")) {
                        String downloads = ele.getElementsByTag("p").first().text().split(".<")[0].split("more than")[1].trim().split(" ")[0].trim().replaceAll(",", "");
                        return Long.parseLong(downloads);
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        //fallback number if the actual number isn't available
        return 1622770000L;
    }
}