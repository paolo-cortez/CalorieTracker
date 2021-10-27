import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}
import java.util.Scanner
import scala.annotation.tailrec
object CalTrackApp {
  var option: Int = 0
  var calories: Int = 0
  var food: String = ""
  var name: String = ""
  val driver = "com.mysql.cj.jdbc.Driver"
  val url = "jdbc:mysql://localhost:3306/caltrackdb"
  val username = "root"
  val password = "root"
  val scanner = new Scanner(System.in)

  def startMenu(): Unit = {
    println("CAL-TRACK: A CALORIE TRACKER")
    println("----------------------------------")
    println("1) Track my calories")
    println("2) Create personal plan")
    println("Enter your option:")
    option = scanner.nextInt()
    if (option == 1) {
      println("----------------------------------")
      println("Please enter your name:")
      name = scanner.next()
      userMenu()
    } else if (option == 2) {
      println("This feature is not yet available")
      startMenu()
    }
  }

  def userMenu() : Unit = {
    println("----------------------------------")
    println(s"Welcome, $name! What would you like to do?")
    println("1) Add food/drink")
    println("2) View my daily log")
    println("Enter your option:")
    option = scanner.nextInt()
    if(option == 1) {
      addEntry()
      println("----------------------------------")
      println("Would you like to add another entry?")
      println("1) Yes")
      println("2) NO, return to menu ")
      println("Enter your option:")
      option = scanner.nextInt()
      if(option == 1) {
        addEntry()
      } else {
        startMenu()
      }
    }
    else if(option == 2) {
      viewDailyLog()
    }
  }
  def addEntry() : Unit = {
    println("----------------------------------")
    println("What would you like to call this entry?")
    food = scanner.next()
    println("----------------------------------")
    println("How many calories was this?")
    calories = scanner.nextInt()
    val connection: Connection = DriverManager.getConnection(url, username, password)
    val query = "INSERT INTO foodlog(user_name, entry_date, entry_time, entry, calories) VALUES (?, now(), now(), ?, ?)"
    val statement : PreparedStatement = connection.prepareStatement(query)
    statement.setString(1, name)
    statement.setString(2, food)
    statement.setInt(3, calories)
    statement.executeUpdate()
    connection.close()
    println("Entry successfully added")
    viewDailyLog()
  }

  def clearLog() : Unit = {
    val connection: Connection = DriverManager.getConnection(url, username, password)
    val query : String = "DELETE FROM foodlog WHERE user_name = ?"
    val statement : PreparedStatement = connection.prepareStatement(query)
    statement.setString(1, name)
    statement.executeUpdate()
    connection.close()
    println(s"Daily log for $name successfully cleared")
    viewDailyLog()
  }

  def viewDailyLog() : Unit = {
    val connection: Connection = DriverManager.getConnection(url, username, password)
    val query: String = "SELECT entry_time AS '   TIME    ', entry AS 'ENTRY', calories AS 'CALORIES' FROM foodlog WHERE user_name = ? UNION ALL SELECT '        ' entry_time, 'TOTAL CALORIES' entry, SUM(calories) from foodlog WHERE user_name = ?"
    val statement: PreparedStatement = connection.prepareStatement(query)
    statement.setString(1, name)
    statement.setString(2, name)
    val rs: ResultSet = statement.executeQuery()
    val rsmd = rs.getMetaData
    val columnCount = rsmd.getColumnCount
    var rowCnt = 0
    val s = new StringBuilder
    println("++++++++++++++++++++++++++++++++++++")
    println(s"DAILY CALORIE LOG: $name ")
    println("------------------------------------")
    while (rs.next()) {
      s.clear()
      if (rowCnt == 0) {
        s.append("| ")
        for (i <- 1 to columnCount) {
          val name = rsmd.getColumnName(i)
          s.append(name)
          s.append(" | ")
        }
        s.append("\n")
      }
      rowCnt += 1
      s.append("| ")
      for (i <- 1 to columnCount) {
        if (i > 1)
          s.append(" | ")
        s.append(rs.getString(i))
      }
      s.append(" |")
      System.out.println(s)
    }
    connection.close()
    println("++++++++++++++++++++++++++++++++++++")
    println("What would you like to do from here?")
    println("1) Return to menu")
    println("2) Add another entry")
    println("3) Reset daily log")
    println("Enter your option:")
    option = scanner.nextInt()
    if (option == 1) {
      startMenu()
    } else if (option == 2) {
      addEntry()
    } else {
      clearLog()
    }
  }

  def main (args : Array[String]): Unit = {
    CalTrackApp.startMenu()
  }
}
