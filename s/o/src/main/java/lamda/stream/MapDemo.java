package lamda.stream;

import java.util.List;
import java.util.stream.Stream;

import lamda.stream.MapDemo.Player;

public class MapDemo {
	
    public static void main(String[] args) {
        List<Employee> list = Employee.getEmpList();
        Stream<Player> players = list.stream().map(e -> new Player(e.id, e.name));
        players.forEach(p -> System.out.println(p.id+ ", "+p.name));
    }
    
    static class Player {
        int id;
        String name;
        Player(int id, String name){
          this.id = id;
          this.name = name;
        }
    }
} 