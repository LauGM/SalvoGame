package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
		System.out.println("Hola Mundo!");
	}

	@Bean
	public PasswordEncoder passwordEncoder() {

		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepo, ShipRepository shipRepository, SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
		return (args) -> {
			// save a couple of players
			Player player1=new Player("pepe@gmail.com", passwordEncoder().encode("1234"));
			playerRepository.save(player1);
			Player player2=new Player("dos@gmail.com",passwordEncoder().encode("1234"));
			playerRepository.save(player2);

			Game game1=new Game();
			gameRepository.save(game1);
			Game game2=new Game();
			gameRepository.save(game2);

			GamePlayer gp1=new GamePlayer(game1, player1);
      		gamePlayerRepo.save(gp1);
			GamePlayer gp2=new GamePlayer(game1, player2);
			gamePlayerRepo.save(gp2);
			GamePlayer gp3=new GamePlayer(game2, player1);
			gamePlayerRepo.save(gp3);

			String fighter = "Fighter";
			String cruiser = "Cruiser";
			String bomber = "Bomber";
			String destroyer = "Destroyer";
			String starfighter = "StarFighter";

			List<String> locations1 = new ArrayList<>();
			locations1.add("H1");
			locations1.add("H2");
			locations1.add("H3");
			locations1.add("H4");

			Ship ship1 = new Ship(destroyer, locations1, gp1);
			Ship ship2 = new Ship(bomber, Arrays.asList("E1","F1","G1"), gp1);
			Ship ship3 = new Ship(fighter, Arrays.asList("B4","B5","B6"), gp1);
			Ship ship4 = new Ship(cruiser, Arrays.asList("A4","A5","A6","A7","A8"), gp1);
			Ship ship5 = new Ship(starfighter, Arrays.asList("J4","J5"), gp1);
			Ship ship6 = new Ship(destroyer, Arrays.asList("B5","C5","D5","E5"), gp2);
			Ship ship7 = new Ship(fighter, Arrays.asList("F1","F2","F3"), gp2);
			Ship ship8 = new Ship(cruiser, Arrays.asList("H2","H3","H4","H5","H6"),gp2);
			Ship ship9 = new Ship(bomber, Arrays.asList("A4","B4","C4"), gp2);
			Ship ship10 = new Ship(starfighter, Arrays.asList("I4","I5"), gp2);

			shipRepository.save(ship1);
			shipRepository.save(ship2);
			shipRepository.save(ship3);
			shipRepository.save(ship4);
			shipRepository.save(ship5);
			shipRepository.save(ship6);
			shipRepository.save(ship7);
			shipRepository.save(ship8);
			shipRepository.save(ship9);
			shipRepository.save(ship10);


			Salvo salvo1 = new Salvo(gp1,1,Arrays.asList("C2","J7","F1","F2","F3"));
			Salvo salvo2 = new Salvo(gp2,1,Arrays.asList("A2","B3"));
			Salvo salvo3 = new Salvo(gp1,2,Arrays.asList("D6","B1","B5","C5","D5"));
			Salvo salvo4 = new Salvo(gp2,2,Arrays.asList("C2","J7"));

			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);

			Double valor1=1.0D;
			Double valor2=0.5D;

			Score score1 = new Score(game1,player1,valor1,new Date());
			Score score2 = new Score(game1,player2,valor2,new Date());


			scoreRepository.save(score1);
			scoreRepository.save(score2);

    	};
	}

}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {
	@Autowired
	PlayerRepository playerRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName-> {
			Player player = playerRepository.findByUserName(inputName);
			if (player != null) {
				System.out.println("entré :)");
				return new User(player.getUserName(), player.getPassword(),
						AuthorityUtils.createAuthorityList("USER"));
			} else {
				System.out.println("no existe ese jugador! :(");
				throw new UsernameNotFoundException("Unknown user: " + inputName);
			}
		});
	}
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/web/**").permitAll()
                .antMatchers("/api/game_view/**").hasAuthority("USER")
                .antMatchers("/api/games").permitAll();
        http.formLogin()
                .usernameParameter("username")
                .passwordParameter("password")
                .loginPage("/api/login");
        http.logout().logoutUrl("/api/logout");
        // turn off checking for CSRF tokens
        http.csrf().disable();
        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));
        // if login is successful, just clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));
        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));
        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }
    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }


    /*@Bean
    @Override
    public UserDetailsService userDetailsService() {
        UserDetails user =
                User.withDefaultPasswordEncoder()
                        .username("username")
                        .password("password")
                        .roles("USER")
                        .build();

        return new InMemoryUserDetailsManager(user);
    }*/
}
