package com.civicpulse.insights;

import com.civicpulse.account.UserDao;
import com.civicpulse.complaints.*;
import com.civicpulse.location.JaipurPlaces;
import com.civicpulse.shared.*;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.springframework.boot.*;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Fictional Jaipur sample complaints for the local demo profile: every zone, service and status,
 * linked issues with high and medium priority, overdue work and anonymous reports.
 */
@Component
@Profile("demo")
@Order(100)
public class DemoReports implements ApplicationRunner {
  private final ComplaintDao dao;
  private final ImageStore images;
  private final UserDao users;
  private final JdbcTemplate jdbc;
  private final ReporterIdentity identity;
  private final JaipurPlaces places;
  private byte[] before, after;

  public DemoReports(
      ComplaintDao dao,
      ImageStore images,
      UserDao users,
      JdbcTemplate jdbc,
      ReporterIdentity identity,
      JaipurPlaces places) {
    this.dao = dao;
    this.images = images;
    this.users = users;
    this.jdbc = jdbc;
    this.identity = identity;
    this.places = places;
  }

  /** reporter: -1 is the demo citizen, 0-11 are sample citizens. */
  private record Seed(
      String ref,
      String code,
      String place,
      int east,
      int north,
      String address,
      String title,
      String description,
      int reporter,
      ComplaintStatus status,
      double daysAgo,
      boolean anonymous,
      String linkTo) {}

  private static final String[] CITIZENS = {
    "Aarav Mathur", "Priya Khandelwal", "Rohit Choudhary", "Sneha Jain", "Mohit Agarwal",
    "Ananya Saxena", "Karan Rathore", "Fatima Sheikh", "Deepak Yadav", "Ritu Sharma",
    "Manish Bairwa", "Sakshi Verma"
  };

  private static final String[][] OFFICERS = {
    {"Street Lighting", "Sunil Saini", "lighting.officer"},
    {"Sanitation", "Imran Qureshi", "sanitation.officer"},
    {"Water Services", "Pooja Choudhary", "water.officer"},
    {"Sewerage and Drainage", "Vikram Singh Shekhawat", "drainage.officer"},
    {"Animal Control", "Mahendra Gurjar", "animal.officer"},
    {"Enforcement and Vigilance", "Neha Agarwal", "enforcement.officer"},
    {"Horticulture", "Mohan Lal Jangid", "parks.officer"},
    {"Public Health", "Kavita Joshi", "health.officer"}
  };

  // Work started, optional progress update, repair submitted.
  private static final Map<String, String[]> NOTES =
      Map.ofEntries(
          Map.entry(
              "road",
              new String[] {
                "Site inspected. Barricades placed and patch work scheduled.",
                "Hot-mix material has arrived; the crew is filling the potholes.",
                "Potholes filled with hot-mix and rolled. The road is safe for traffic."
              }),
          Map.entry(
              "footpath",
              new String[] {
                "Site measured for replacement slabs.",
                "New slabs delivered to the site.",
                "Broken slabs replaced and the footpath levelled."
              }),
          Map.entry(
              "streetlight",
              new String[] {
                "Electrician deputed to check the feeder and fixtures.",
                "Faulty drivers and LED fittings identified.",
                "Replaced the LED fittings and repaired the feeder cable. Lights are working."
              }),
          Map.entry(
              "garbage",
              new String[] {
                "Sanitation vehicle scheduled for the ward.",
                "",
                "Garbage lifted, area swept and lime powder sprayed."
              }),
          Map.entry(
              "water",
              new String[] {
                "Water supply team is checking the valve and supply line.",
                "",
                "Valve repaired; supply restored at normal pressure."
              }),
          Map.entry(
              "drain",
              new String[] {
                "Desilting team scheduled.", "", "Drain desilted and a new cover fitted."
              }),
          Map.entry(
              "sewer",
              new String[] {
                "Sewer jetting machine dispatched.",
                "Blockage located near the manhole junction.",
                "Line cleared with the jetting machine; the overflow has stopped."
              }),
          Map.entry(
              "waterlogging",
              new String[] {
                "Pump set deployed to drain the water.", "", "Water pumped out and the inlet cleaned."
              }),
          Map.entry(
              "animal",
              new String[] {
                "Animal catching team alerted.", "", "Animals moved to the municipal shelter."
              }),
          Map.entry(
              "carcass",
              new String[] {
                "Carcass removal vehicle dispatched.", "", "Carcass removed and the area disinfected."
              }),
          Map.entry(
              "encroachment",
              new String[] {
                "Enforcement team issued notices to the vendors.",
                "Vendors given one day to clear the footpath.",
                "Encroachment removed and the footpath cleared."
              }),
          Map.entry(
              "hoarding",
              new String[] {
                "Survey of hoardings ordered.", "", "Illegal hoardings removed from the divider."
              }),
          Map.entry(
              "park",
              new String[] {
                "Horticulture supervisor inspected the park.", "", "Repairs completed in the park."
              }),
          Map.entry(
              "tree",
              new String[] {
                "Tree cutting team with a crane dispatched.",
                "",
                "Branches trimmed away from the power lines and the road cleared."
              }),
          Map.entry(
              "toilet",
              new String[] {
                "Cleaning contractor instructed.", "", "Toilet cleaned and water supply restored."
              }),
          Map.entry(
              "mosquito",
              new String[] {
                "Anti-larval team scheduled for the area.",
                "",
                "Fogging and anti-larval spraying completed in all lanes."
              }));

  private static Seed seed(
      String ref,
      String code,
      String place,
      int east,
      int north,
      String address,
      String title,
      String description,
      int reporter,
      ComplaintStatus status,
      double daysAgo) {
    return new Seed(
        ref, code, place, east, north, address, title, description, reporter, status, daysAgo,
        false, null);
  }

  private static Seed anonymous(Seed s) {
    return new Seed(
        s.ref(), s.code(), s.place(), s.east(), s.north(), s.address(), s.title(), s.description(),
        s.reporter(), s.status(), s.daysAgo(), true, s.linkTo());
  }

  private static Seed linked(Seed s, String root) {
    return new Seed(
        s.ref(), s.code(), s.place(), s.east(), s.north(), s.address(), s.title(), s.description(),
        s.reporter(), s.status(), s.daysAgo(), s.anonymous(), root);
  }

  private static final ComplaintStatus REPORTED = ComplaintStatus.REPORTED,
      ASSIGNED = ComplaintStatus.ASSIGNED,
      IN_PROGRESS = ComplaintStatus.IN_PROGRESS,
      RESOLVED = ComplaintStatus.RESOLVED,
      CLOSED = ComplaintStatus.CLOSED,
      REJECTED = ComplaintStatus.REJECTED;

  private static List<Seed> seeds() {
    String tonk = "Tonk Road, near Gopalpura flyover, Jaipur";
    String tonkTitle = "Deep potholes on Tonk Road near Gopalpura flyover";
    String tonkText =
        "Several deep potholes in the left lane just before the Gopalpura flyover. Two-wheelers"
            + " swerve into traffic to avoid them and it gets worse after rain.";
    String johari = "Johari Bazaar, near Sanganeri Gate, Jaipur";
    var list = new ArrayList<Seed>();
    // High priority: six citizens reported the same potholes (one anonymously).
    list.add(seed("CP-DEMO-1001", "road", "Tonk Road", 0, -650, tonk, tonkTitle, tonkText, 0, IN_PROGRESS, 9));
    for (int i = 1; i <= 5; i++) {
      var s =
          seed(
              "CP-DEMO-100" + (i + 1), "road", "Tonk Road", 2 * i, -650 + 3 * i, tonk, tonkTitle,
              "Same potholes near the Gopalpura flyover. A scooter rider fell here this week.", i,
              IN_PROGRESS, 9 - i * 0.8);
      list.add(linked(i == 3 ? anonymous(s) : s, "CP-DEMO-1001"));
    }
    // Medium priority clusters.
    list.add(
        anonymous(
            seed(
                "CP-DEMO-1011", "garbage", "Johari Bazaar", 0, -150, johari,
                "Garbage heap outside Johari Bazaar shops",
                "Garbage has piled up on the pavement outside the shops for two days. Stray cattle"
                    + " are spreading it onto the road and it smells badly.",
                -1, REPORTED, 1.6)));
    list.add(
        linked(
            seed(
                "CP-DEMO-1012", "garbage", "Johari Bazaar", 3, -148, johari,
                "जौहरी बाज़ार में दुकानों के बाहर कचरे का ढेर",
                "दो दिन से दुकानों के बाहर कचरा पड़ा है। बदबू आ रही है और आवारा पशु कचरा सड़क पर फैला रहे हैं।",
                1, REPORTED, 1.2),
            "CP-DEMO-1011"));
    list.add(
        linked(
            seed(
                "CP-DEMO-1013", "garbage", "Johari Bazaar", -2, -152, johari,
                "Overflowing garbage near Johari Bazaar market",
                "The municipal bin is full and garbage is overflowing onto the footpath.", 9,
                REPORTED, 0.7),
            "CP-DEMO-1011"));
    list.add(
        seed(
            "CP-DEMO-1021", "sewer", "Ramganj Bazaar", 40, 10, "Ramganj Bazaar main road, Jaipur",
            "रामगंज बाज़ार में सीवर का गंदा पानी सड़क पर",
            "सुबह से सीवर का पानी सड़क पर बह रहा है। दुकानदारों और पैदल चलने वालों को परेशानी हो रही है।",
            7, ASSIGNED, 1.1));
    list.add(
        linked(
            seed(
                "CP-DEMO-1022", "sewer", "Ramganj Bazaar", 44, 12, "Ramganj Bazaar main road, Jaipur",
                "Sewer overflowing on Ramganj Bazaar road",
                "Dirty sewer water is flowing on the main road since morning.", 8, ASSIGNED, 0.9),
            "CP-DEMO-1021"));
    list.add(
        seed(
            "CP-DEMO-1031", "animal", "Sindhi Camp", 60, -40, "Sindhi Camp bus stand exit, Jaipur",
            "Stray cattle blocking the Sindhi Camp bus stand exit",
            "A group of stray cattle sits at the bus stand exit every evening and buses cannot turn"
                + " safely.",
            4, REPORTED, 1));
    list.add(
        linked(
            seed(
                "CP-DEMO-1032", "animal", "Sindhi Camp", 64, -38, "Sindhi Camp bus stand exit, Jaipur",
                "Cattle on the road outside Sindhi Camp",
                "Cattle are blocking traffic at the bus stand gate.", 10, REPORTED, 0.6),
            "CP-DEMO-1031"));
    // The demo citizen's own reports.
    list.add(
        seed(
            "CP-DEMO-1101", "road", "Malviya Nagar", 120, 80, "Sector 3 market, Malviya Nagar, Jaipur",
            "Pothole near Malviya Nagar Sector 3 market",
            "A large pothole at the market entrance fills with water and has damaged several"
                + " vehicles.",
            -1, RESOLVED, 6));
    list.add(
        seed(
            "CP-DEMO-1102", "streetlight", "Vaishali Nagar", -200, 60,
            "Main road near Amrapali Circle, Vaishali Nagar, Jaipur",
            "Four streetlights dark on Vaishali Nagar main road",
            "Four consecutive streetlights have not worked for a week. The stretch is very dark"
                + " for women walking home.",
            -1, IN_PROGRESS, 2));
    list.add(
        seed(
            "CP-DEMO-1103", "water", "Mansarovar", 150, -250, "Sector 7, Mansarovar, Jaipur",
            "No water supply in Mansarovar Sector 7",
            "There has been no water supply since yesterday morning in the whole lane.", -1, CLOSED,
            12));
    list.add(
        seed(
            "CP-DEMO-1104", "drain", "Raja Park", -60, 90, "Gali No. 4, Raja Park, Jaipur",
            "Blocked drain near Raja Park market",
            "The drain is blocked with plastic and water is backing up into the lane.", -1,
            ASSIGNED, 5));
    list.add(
        seed(
            "CP-DEMO-1105", "hoarding", "Ajmer Road (Purani Chungi)", 80, 30,
            "Ajmer Road near Purani Chungi, Jaipur", "Hoarding leaning over the Ajmer Road footpath",
            "A large hoarding frame is leaning over the footpath after last night's storm.", -1,
            REJECTED, 8));
    // Roads work for the demo officer.
    list.add(
        seed(
            "CP-DEMO-1201", "road", "C-Scheme", 90, -60, "Ashok Marg, C-Scheme, Jaipur",
            "Road dug up after pipeline work in C-Scheme",
            "The road was dug for a pipeline and never restored. Gravel is spread across the lane.",
            2, ASSIGNED, 1));
    list.add(
        seed(
            "CP-DEMO-1202", "footpath", "SMS Hospital", 40, 50, "SMS Hospital main gate, Jaipur",
            "Footpath slabs broken near SMS Hospital gate",
            "Broken slabs outside the hospital gate are dangerous for patients and wheelchairs.", 5,
            IN_PROGRESS, 4));
    list.add(
        seed(
            "CP-DEMO-1203", "road", "Statue Circle", 30, 40, "Statue Circle, C-Scheme, Jaipur",
            "Speed breaker damaged near Statue Circle",
            "The speed breaker has broken edges with exposed metal.", 6, CLOSED, 20));
    list.add(
        seed(
            "CP-DEMO-1204", "road", "Jawahar Circle", -80, 60, "JLN Marg near Jawahar Circle, Jaipur",
            "Road surface caved in near Jawahar Circle",
            "Part of the road surface has caved in near the bus stop.", 11, CLOSED, 15));
    list.add(
        seed(
            "CP-DEMO-1205", "road", "Agra Road", 120, -20, "Agra Road near Jamdoli, Jaipur",
            "Potholes on Agra Road near Jamdoli",
            "A series of potholes on the highway service lane; trucks are breaking down here.", 10,
            REPORTED, 0.3));
    // Awaiting administrator review.
    list.add(
        seed(
            "CP-DEMO-1301", "streetlight", "Hawa Mahal", 80, -40, "Hawa Mahal bus stop, Badi Chaupar, Jaipur",
            "Streetlight not working near Hawa Mahal bus stop",
            "The light pole at the bus stop is dark. Tourists wait here after sunset.", 3, REPORTED,
            0.5));
    list.add(
        seed(
            "CP-DEMO-1302", "tree", "Bani Park", -50, 70, "Shiv Marg, Bani Park, Jaipur",
            "Tree fallen across the lane in Bani Park",
            "A neem tree fell across the lane in last night's storm. Cars cannot pass.", 9, REPORTED,
            0.15));
    list.add(
        seed(
            "CP-DEMO-1303", "mosquito", "Jhotwara", 100, 50, "Near Jhotwara police station, Jaipur",
            "Stagnant water breeding mosquitoes in Jhotwara",
            "An empty plot is filled with stagnant water and mosquitoes. Several dengue cases in"
                + " the colony.",
            10, REPORTED, 2));
    list.add(
        seed(
            "CP-DEMO-1304", "encroachment", "Chandpole Bazaar", 30, 10, "Chandpole Bazaar, Jaipur",
            "Shops encroaching the footpath at Chandpole Bazaar",
            "Shops have extended their counters onto the footpath. Pedestrians walk on the road.", 1,
            REPORTED, 6));
    list.add(
        seed(
            "CP-DEMO-1305", "park", "Central Park", 60, 80, "Central Park jogging track, Jaipur",
            "Jogging track lights broken in Central Park",
            "Half of the lights on the jogging track have been broken for weeks.", 8, REPORTED, 1));
    list.add(
        anonymous(
            seed(
                "CP-DEMO-1306", "toilet", "Sindhi Camp", -40, 50, "Sindhi Camp bus stand, Jaipur",
                "Public toilet near Sindhi Camp bus stand locked",
                "The public toilet is locked during the day and travellers have nowhere to go.", 11,
                REPORTED, 1)));
    list.add(
        seed(
            "CP-DEMO-1307", "carcass", "VKI Area", 50, -70, "Sikar Road near VKI Road No. 14, Jaipur",
            "Dead cow on Sikar Road near VKI",
            "A dead cow is lying on the service lane and traffic is swerving around it.", 4,
            REPORTED, 0.25));
    list.add(
        seed(
            "CP-DEMO-1308", "water", "Jagatpura", 70, 40, "Near Jagatpura railway crossing, Jaipur",
            "जगतपुरा में नल से गंदा पानी आ रहा है",
            "तीन दिन से नल से मटमैला और बदबूदार पानी आ रहा है। बच्चे बीमार पड़ रहे हैं।", 0,
            REPORTED, 1));
    list.add(
        seed(
            "CP-DEMO-1309", "waterlogging", "Durgapura", 30, -60, "Durgapura railway underpass, Jaipur",
            "Waterlogging under the Durgapura railway underpass",
            "Knee-deep water collects under the underpass after every rain and vehicles get stuck.",
            7, REPORTED, 2));
    // Work across departments.
    list.add(
        seed(
            "CP-DEMO-1401", "garbage", "Murlipura", 80, 50, "Scheme No. 2, Murlipura, Jaipur",
            "Garbage not collected for three days in Murlipura",
            "The door-to-door vehicle has not come for three days.", 2, ASSIGNED, 2));
    list.add(
        seed(
            "CP-DEMO-1402", "streetlight", "Rajasthan University", -40, -90, "JLN Marg near Rajasthan University, Jaipur",
            "Dark stretch on JLN Marg near Rajasthan University",
            "Streetlights between the university gate and the bus stop are off.", 5, CLOSED, 10));
    list.add(
        seed(
            "CP-DEMO-1403", "sewer", "Galta Gate", 30, 20, "Galta Gate chowk, Jaipur",
            "Sewer overflowing near Galta Gate",
            "Sewage is overflowing from a manhole at the chowk.", 6, IN_PROGRESS, 1));
    list.add(
        seed(
            "CP-DEMO-1404", "animal", "Pratap Nagar", -60, 40, "Sector 8, Pratap Nagar, Jaipur",
            "Stray dogs chasing two-wheelers in Pratap Nagar",
            "A pack of stray dogs chases two-wheelers at night near the park.", 8, CLOSED, 18));
    list.add(
        seed(
            "CP-DEMO-1405", "tree", "Shastri Nagar", 40, -30, "Main road, Shastri Nagar, Jaipur",
            "Branches touching power lines at Shastri Nagar",
            "Tree branches are touching the overhead power lines and spark during wind.", 9,
            RESOLVED, 3));
    list.add(
        seed(
            "CP-DEMO-1406", "water", "Queens Road", 50, 20, "Queens Road, Jaipur",
            "Water pipeline leaking on Queens Road",
            "Clean water has been gushing from a pipeline joint for two days.", 10, CLOSED, 7));
    list.add(
        seed(
            "CP-DEMO-1407", "garbage", "Amer", -300, -200, "Amer Road near Kanak Ghati, Jaipur",
            "आमेर रोड पर कचरा जलाया जा रहा है",
            "रोज़ शाम को सड़क किनारे कचरा जलाया जाता है और धुएं से सांस लेने में दिक्कत होती है।", 11,
            IN_PROGRESS, 1));
    list.add(
        seed(
            "CP-DEMO-1408", "hoarding", "JLN Marg", 20, 40, "JLN Marg divider, Jaipur",
            "Illegal hoardings on the JLN Marg divider",
            "Dozens of illegal hoardings are tied to the divider railings.", 0, ASSIGNED, 3));
    list.add(
        seed(
            "CP-DEMO-1409", "park", "Sitapura", 90, 120, "Community park, Sitapura, Jaipur",
            "Swings broken in Sitapura community park",
            "Two swings are broken and children are getting hurt.", 3, REPORTED, 4));
    list.add(
        seed(
            "CP-DEMO-1410", "footpath", "Sanganer", 50, 30, "Sanganer bus stand, Jaipur",
            "Footpath cover missing near Sanganer bus stand",
            "A drain cover on the footpath is missing; it is an open hole.", 4, REPORTED, 1));
    list.add(
        seed(
            "CP-DEMO-1411", "mosquito", "Vidhyadhar Nagar", -50, 60, "Sector 6, Vidhyadhar Nagar, Jaipur",
            "Fogging needed in Vidhyadhar Nagar Sector 6",
            "There are many mosquitoes in the sector and no fogging this season.", 5, CLOSED, 9));
    list.add(
        seed(
            "CP-DEMO-1412", "drain", "Jhalana Doongri", 40, -50, "Near the government school, Jhalana Doongri, Jaipur",
            "Open drain without a cover near the school",
            "An open drain runs beside the school gate. Children could fall in.", 6, ASSIGNED, 1));
    list.add(
        seed(
            "CP-DEMO-1413", "toilet", "Transport Nagar", 30, 30, "Transport Nagar truck stand, Jaipur",
            "Public toilet not cleaned at Transport Nagar",
            "The public toilet has not been cleaned for days.", 7, CLOSED, 5));
    list.add(
        seed(
            "CP-DEMO-1414", "encroachment", "Adarsh Nagar", 60, -40, "Adarsh Nagar market road, Jaipur",
            "Illegal stalls blocking the road outside Adarsh Nagar market",
            "Stalls have taken over half of the road in the evening rush.", 8, IN_PROGRESS, 6));
    list.add(
        seed(
            "CP-DEMO-1415", "waterlogging", "Kalwar Road", 40, 30, "Kalwar Road near Govindpura, Jaipur",
            "Water standing on Kalwar Road after rain",
            "Rainwater has not drained for two days and the road is flooded.", 9, RESOLVED, 2));
    list.add(
        seed(
            "CP-DEMO-1416", "streetlight", "Mansarovar", -120, 200, "Shipra Path, Mansarovar, Jaipur",
            "Streetlight on during the day on Shipra Path",
            "Streetlights stay switched on throughout the day, wasting electricity.", 3, ASSIGNED, 1.5));
    list.add(
        seed(
            "CP-DEMO-1417", "garbage", "Jagatpura", -80, -60, "Mahal Road, Jagatpura, Jaipur",
            "Garbage dumped on the roadside on Mahal Road",
            "Construction debris and household waste are dumped on the roadside every night.", 2,
            CLOSED, 11));
    list.add(
        seed(
            "CP-DEMO-1418", "road", "Vidhyadhar Nagar", 70, -40, "Central Spine, Vidhyadhar Nagar, Jaipur",
            "Manhole cover sunk on Central Spine road",
            "The manhole cover has sunk below the road level and vehicles jump over it.", 1,
            CLOSED, 14));
    return list;
  }

  public void run(ApplicationArguments args) throws Exception {
    if (dao.count("all", null, null, null) > 0) return;
    long citizen = users.findByEmail("citizen@civicpulse.demo").orElseThrow().id();
    long roadsOfficer = users.findByEmail("officer@civicpulse.demo").orElseThrow().id();
    long admin = users.findByEmail("admin@civicpulse.demo").orElseThrow().id();
    users.updateDisplayName(citizen, "Riya Sharma");
    users.updateDisplayName(roadsOfficer, "Rakesh Meena");
    users.updateDisplayName(admin, "Anita Rathore");
    String hash = users.findById(citizen).orElseThrow().passwordHash();
    long[] people = new long[CITIZENS.length];
    for (int i = 0; i < people.length; i++)
      people[i] =
          users.create(CITIZENS[i], "sample" + (i + 1) + "@civicpulse.demo", hash, Role.CITIZEN).id();
    var officers = new HashMap<String, long[]>();
    var officerNames = new HashMap<Long, String>();
    officers.put("Roads and Infrastructure", new long[] {roadsOfficer});
    officerNames.put(roadsOfficer, "Rakesh Meena");
    for (var o : OFFICERS) {
      long id = users.create(o[1], o[2] + "@civicpulse.demo", hash, Role.OFFICER).id();
      jdbc.update(
          "UPDATE users SET department_id=(SELECT id FROM departments WHERE name=?) WHERE id=?",
          o[0],
          id);
      officers.put(o[0], new long[] {id});
      officerNames.put(id, o[1]);
    }
    before = png("before.jpg");
    after = png("after.jpg");
    var categories = new HashMap<String, Category>();
    for (var c : dao.categories()) categories.put(c.getCode(), c);
    var departments = new HashMap<Long, String>();
    jdbc.query(
        "SELECT id,name FROM departments",
        (org.springframework.jdbc.core.RowCallbackHandler)
            r -> departments.put(r.getLong(1), r.getString(2)));
    var ids = new HashMap<String, Long>();
    Instant now = Instant.now();
    for (var s : seeds()) {
      var category = categories.get(s.code());
      var place =
          places.all().stream().filter(p -> p.name().equals(s.place())).findFirst().orElseThrow();
      double lat = place.latitude() + s.north() / 111_320.0;
      double lon = place.longitude() + s.east() / (111_320.0 * Math.cos(Math.toRadians(lat)));
      var f = new ComplaintForm();
      f.setTitle(s.title());
      f.setDescription(s.description());
      f.setAddress(s.address());
      f.setCategoryId(category.id());
      f.setLatitude(lat);
      f.setLongitude(lon);
      long account = s.reporter() < 0 ? citizen : people[s.reporter()];
      long reporter = s.anonymous() ? identity.anonymousAccount() : account;
      long id = dao.create(f, reporter, s.ref(), s.anonymous(), identity.hash(account));
      ids.put(s.ref(), id);
      if (s.linkTo() != null) dao.link(id, ids.get(s.linkTo()));
      boolean photo = s.code().equals("road") || s.code().equals("footpath");
      if (photo) images.add(id, reporter, "ORIGINAL", before);
      long officer = officers.get(departments.get(category.departmentId()))[0];
      lifecycle(id, s, reporter, officer, officerNames.get(officer), admin, photo, now);
    }
    // Neighbours supporting active issues.
    support(ids.get("CP-DEMO-1001"), people[6], people[7], people[8], citizen);
    support(ids.get("CP-DEMO-1011"), people[2], people[5]);
    support(ids.get("CP-DEMO-1021"), people[0], people[3], citizen);
    support(ids.get("CP-DEMO-1301"), people[4], people[9]);
    support(ids.get("CP-DEMO-1309"), people[1], people[2], people[3], citizen);
    support(ids.get("CP-DEMO-1302"), people[5]);
  }

  /** Writes a realistic, time-stamped history and the final state for one sample report. */
  private void lifecycle(
      long id,
      Seed s,
      long reporter,
      long officer,
      String officerName,
      long admin,
      boolean photo,
      Instant now) {
    Instant created = now.minus(Duration.ofMinutes(Math.round(s.daysAgo() * 1440)));
    long ageMinutes = Math.max(30, Duration.between(created, now).toMinutes() - 20);
    // Typical offsets (assign 3h, start 18h, update 30h, repair 44h, confirm 64h), faster or slower
    // per report, and compressed for reports younger than that.
    double pace = 0.5 + (Math.floorMod(s.ref().hashCode(), 100) / 100.0) * 1.3;
    double scale = Math.min(pace, ageMinutes / (64.0 * 60));
    java.util.function.DoubleFunction<Instant> at =
        hours -> created.plus(Duration.ofMinutes(Math.round(hours * 60 * scale)));
    String[] notes = NOTES.get(s.code());
    var target = s.status();
    event(
        id,
        reporter,
        "REPORTED",
        null,
        REPORTED,
        s.anonymous() ? "Anonymous report submitted for review." : "Report submitted for review.",
        created);
    Instant updated = created, resolved = null, closed = null;
    Long assigned = null;
    if (target == REJECTED) {
      updated = at.apply(5);
      event(
          id,
          admin,
          "REJECTED",
          REPORTED,
          REJECTED,
          "The hoarding was removed by its owner before inspection. Report again if it returns.",
          updated);
    }
    if (target.compareTo(ASSIGNED) >= 0 && target != REJECTED) {
      assigned = officer;
      updated = at.apply(3);
      event(
          id,
          admin,
          "ASSIGNED",
          REPORTED,
          ASSIGNED,
          "Location verified on the map. Assigned to " + officerName + ".",
          updated);
    }
    if (target.compareTo(IN_PROGRESS) >= 0 && target != REJECTED) {
      updated = at.apply(18);
      event(id, officer, "WORK_STARTED", ASSIGNED, IN_PROGRESS, notes[0], updated);
      if (!notes[1].isEmpty()) {
        updated = at.apply(30);
        event(id, officer, "PROGRESS_UPDATE", IN_PROGRESS, IN_PROGRESS, notes[1], updated);
      }
    }
    if (target.compareTo(RESOLVED) >= 0 && target != REJECTED) {
      updated = resolved = at.apply(44);
      if (photo) images.add(id, officer, "REPAIR", after);
      event(id, officer, "REPAIR_SUBMITTED", IN_PROGRESS, RESOLVED, notes[2], updated);
    }
    if (target == CLOSED) {
      updated = closed = at.apply(64);
      event(
          id,
          reporter,
          "CITIZEN_CONFIRMED",
          RESOLVED,
          CLOSED,
          "Citizen verified the repair and closed the report.",
          updated);
    }
    jdbc.update(
        "UPDATE complaints SET status=?,assigned_officer_id=?,created_at=?,updated_at=?,"
            + "resolved_at=?,closed_at=? WHERE id=?",
        target.name(),
        assigned,
        Timestamp.from(created),
        Timestamp.from(updated),
        resolved == null ? null : Timestamp.from(resolved),
        closed == null ? null : Timestamp.from(closed),
        id);
  }

  private void event(
      long id,
      long actor,
      String action,
      ComplaintStatus from,
      ComplaintStatus to,
      String note,
      Instant at) {
    jdbc.update(
        "INSERT INTO complaint_events(complaint_id,actor_id,action,from_status,to_status,note,"
            + "created_at) VALUES(?,?,?,?,?,?,?)",
        id,
        actor,
        action,
        from == null ? null : from.name(),
        to.name(),
        note,
        Timestamp.from(at));
  }

  private void support(long id, long... citizens) {
    for (long c : citizens)
      jdbc.update("INSERT INTO complaint_supports(complaint_id,citizen_id) VALUES(?,?)", id, c);
  }

  private byte[] png(String name) throws Exception {
    try (var in =
        new org.springframework.core.io.ClassPathResource("demo/photos/" + name).getInputStream()) {
      var image = javax.imageio.ImageIO.read(in);
      var out = new java.io.ByteArrayOutputStream();
      javax.imageio.ImageIO.write(image, "png", out);
      return out.toByteArray();
    }
  }
}
