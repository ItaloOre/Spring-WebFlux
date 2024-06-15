package com.webflux.papuitalo.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.webflux.papuitalo.app.models.Comentarios;
import com.webflux.papuitalo.app.models.Usuario;
import com.webflux.papuitalo.app.models.UsuarioComentarios;



@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		//this.ejemploIterable();
		//this.ejemploFlatMap();
		//this.ejemploToString();
		//this.ejemploCollectList();
		//this.ejemploUsuarioComentariosFlatMap();
		//this.ejemploUsuarioComentariosZipWith();
		//this.ejemploUsuarioComentariosZipWith2();
		//this.ejemploZipWithRangos();
		//this.ejemploInterval();
		//this.ejemploDelayElements();
		//this.ejemploIntervalInfinito();
		//this.ejemploIntervalDesdeCreate();
		this.ejemploContraPresion();
	
	}//run
	
	public void ejemploContraPresion() {
		
		Flux.range(1, 10)
		.log()
		//.limitRate(5)
		.subscribe(new Subscriber<Integer>() {
			
			private Subscription s;
			
			private Integer limite = 5;
			private Integer consumido = 0;

			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				s.request(limite);
			}

			@Override
			public void onNext(Integer t) {
				log.info(t.toString());
				consumido++;
				if(consumido == limite) {
					consumido = 0;
					s.request(limite);
				}
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}
		});
	}

	
	public void ejemploIntervalDesdeCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				private Integer contador = 0;

				@Override
				public void run() {
					emitter.next(++contador);
					if (contador == 10) {
						timer.cancel();
						emitter.complete();
					}

					if (contador == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
					}

				}
			}, 1000, 1000);
		}).subscribe(next -> log.info(next.toString()), error -> log.error(error.getMessage()),
				() -> log.info("Hemos terminado"));
	}

	public void ejemploIntervalInfinito() throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1)).doOnTerminate(latch::countDown).flatMap(i -> {
			if (i >= 5) {
				return Flux.error(new InterruptedException("Solo hasta 5!"));
			}
			return Flux.just(i);
		}).map(i -> "Hola " + i).retry(2).subscribe(s -> log.info(s), e -> log.error(e.getMessage()));

		latch.await();
	}

	public void ejemploDelayElements() {
		Flux<Integer> rango = Flux.range(1, 12).delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		rango.blockLast();
	}

	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

		rango.zipWith(retraso, (ra, re) -> ra).doOnNext(i -> log.info(i.toString())).blockLast();
	}
	
	private void ejemploZipWithRangos() {
		Flux.just(1,2,3,4)
		.map(i -> (i*2))
		.zipWith(Flux.range(0, 4), (uno,dos) -> 
		String.format("Primer Flux: %d,Segundo Flux: %d", uno,dos))
		.subscribe(texto -> log.info(texto));
	}

	public void ejemploUsuarioComentariosZipWith2() throws Exception {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> crearUsuario());
		
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentarios("Hola sobrino!");
			comentarios.addComentarios("Que me cuentas bandido!");
			comentarios.addComentarios("Todo en orden!");
			return comentarios;
		});
		
		usuarioMono.zipWith(comentariosUsuarioMono).map(tuple -> {
			Usuario u = tuple.getT1();
			Comentarios c = tuple.getT2();
			return new UsuarioComentarios(u, c);
		});
	}
	
	public void ejemploUsuarioComentariosZipWith() throws Exception {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> crearUsuario());
		
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentarios("Hola sobrino!");
			comentarios.addComentarios("Que me cuentas bandido!");
			comentarios.addComentarios("Todo en orden!");
			return comentarios;
		});
		
		usuarioMono.zipWith(comentariosUsuarioMono, (usuario,comentarios) -> 
		new UsuarioComentarios(usuario, comentarios))
		.subscribe(uc -> log.info(uc.toString()));
	}
	
	private Usuario crearUsuario() {
		return new Usuario("Italo", "Ore");
	}
	
	public void ejemploUsuarioComentariosFlatMap() throws Exception {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> crearUsuario());
		
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentarios("Hola sobrino!");
			comentarios.addComentarios("Que me cuentas bandido!");
			comentarios.addComentarios("Todo en orden!");
			return comentarios;
		});
		
		usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioComentarios(u, c)))
		.subscribe(uc -> log.info(uc.toString()));
	}
	
	public void ejemploCollectList() throws Exception {
		
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Mateo", "Segura"));
		usuariosList.add(new Usuario("Marcos", "Ramirez"));
		usuariosList.add(new Usuario("Lucas", "Garcia"));
		usuariosList.add(new Usuario("Juan", "Garmendia"));
		usuariosList.add(new Usuario("Italo", "Ore"));
		usuariosList.add(new Usuario("Arles", "Ore"));
		
		Flux.fromIterable(usuariosList)
		.collectList()
		.subscribe(lista -> {
			lista.forEach(item -> {
				log.info(item.toString());
			});	
		});
				
	}//fin
	

	public void ejemploToString() throws Exception {
		
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Mateo", "Segura"));
		usuariosList.add(new Usuario("Marcos", "Ramirez"));
		usuariosList.add(new Usuario("Lucas", "Garcia"));
		usuariosList.add(new Usuario("Juan", "Garmendia"));
		usuariosList.add(new Usuario("Italo", "Ore"));
		usuariosList.add(new Usuario("Arles", "Ore"));
		

		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if(nombre.toUpperCase().contains("Ore".toUpperCase())) {
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				})
				.map(nombre -> {
					return nombre.toLowerCase();
					}).
				subscribe(u -> log.info(u.toString()));
	}//fin
	
	public void ejemploFlatMap() throws Exception {
		
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Mateo Segura");
		usuariosList.add("Marcos Ramirez");
		usuariosList.add("Lucas Garcia");
		usuariosList.add("Juan Garmendia");
		usuariosList.add("Italo Ore");
		usuariosList.add("Arles Ore");
		
		Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if(usuario.getApellido().equalsIgnoreCase("Ore")) {
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				})
				.doOnNext(usuario -> {
					if(usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					} 
					System.out.println(usuario);
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
					}).
				subscribe(u -> log.info(u.toString()));
	}//fin
	
	public void ejemploIterable() throws Exception {
		
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Mateo Segura");
		usuariosList.add("Marcos Ramirez");
		usuariosList.add("Lucas Garcia");
		usuariosList.add("Juan Garmendia");
		usuariosList.add("Italo Ore");
		usuariosList.add("Arles Ore");
		
		/*
		 * Flux<String> nombres =
		 * Flux.just("Mateo Segura","Marcos Ramirez","Lucas Garcia",
		 * "Juan Garmendia","Italo Ore","Arles Ore");
		 */
				
		Flux<String> nombres = Flux.fromIterable(usuariosList); 
				
		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getApellido().equalsIgnoreCase("Ore"))
				.doOnNext(usuario -> {
					if(usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					} 
					System.out.println(usuario);
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
					});
		
		usuarios.subscribe(e -> log.info(e.toString()),
			error -> log.error(error.getMessage()),
			new Runnable() {
				@Override
				public void run() {
					log.info("Ha finalizado la ejecucion del observable con exito!");
				}
			});
	
	}//run
}
