# Integrációs tesztelés WebClient használatával

A WebClient a Spring 5-ös verziójában jelent meg és segítségével az alkalmazásunkban 
REST webszolgáltatásokat tudunk meghívni. Létezik egy WebTestClient párja is, amellyel 
WebFluxos alkalmazásokhoz lehet integrációs teszteket írni. A WebFlux a Spring MVC 
mellett a Spring 5-ben bevezetett alternatív webes keretrendszer. Azonban a WebTestClientet 
használhatjuk Spring MVC-s alkalmazások integrációs tesztelésére is.

A WebClient fluent apival rendelkezik, és a használatba vételéhez a `spring-boot-starter-webflux`-ot 
kell felvenni a függőségek közé. Amennyiben Spring MVC-s alkalmazásokhoz szeretnénk integrációs tesztet 
írni vele, akkor az integrációs tesztet úgy kell konfigurálni, hogy egy valós webkonténert indítson el.

A WebTestClientnek van `post()`, `get()`, `put()` és delete()` metódusa, a http metódusoknak megfelelően. 
Ezek használatához a WebTestClientet először injektálnunk kell.

Egy POST kérés esetén a következő lépéseket kell tennünk:

```java
webClient.post().uri("/api/employees")
.bodyValue(new CreateEmployeeCommand(("John Doe")))
.exchange()
.expectStatus().isCreated()
```

A `post()` metódus meghívása után az `uri()` metódusnak kell megadni azt az URI-t, ahová szeretnénk 
elküldeni a kérést. Utána megadhatjuk a kérés törzsét. Ez lehet egy konkrét objektum, amelyet a 
WebTestClient JSON-be fog szerializálni. Az `exchange()` metódus hajtja végre a kérés elküldését. 
Ezután a visszakapott HTTP válaszra tudunk asserteket írni, ebben a kódrészletben például arra, hogy 
a szerver 201-es státuszkódot küldött-e vissza.

Amennyiben az URI-t paraméterezni szeretnénk, arra is van lehetőség. Path variable változó használata 
esetén a következő módon tehetjük ezt meg:

```java
webClient.get().uri("/api/employees/{id}", 1)
```

Itt kapcsos zárójelben megadunk egy placeholdert, amelynek az értékét aztán második paraméterként adjuk 
át az `uri()` metódusnak. A WebClient természetesen ki fogja cserélni a placeholdert a konkrét értékkel.

Query paraméter esetén az URI-t az alábbi módon, builder használatával kell létrehoznunk:

```java
webClient.get().uri(builder -> builder.path("/api/employees").queryParam("prefix", "j").build())
```

Itt a `queryParam()` metódussal kell átadni a paramétereket név - érték párként.

Amennyiben a válasz egy JSON, akkor lehetőség van ezt a konzolra is kiírni:

```java
.expectBody(String.class).value(s -> System.out.println(s))
```

Az `expectBody()` metódusnak adhatjuk meg, hogy a választ Stringként értelmezze. Ezután a `value()` 
metódusnak, amely egy `Consumer`-t vár paraméterül, adhatjuk meg, hogy mi is történjen ezzel a Stringgel 
(ebben a példában kiírjuk a konzolra).

Lehetőség van JSONPath kifejezések használatára is:

```java
.expectBody().jsonPath("name").isEqualTo("John Doe");
```

Ekkor az `expectBody()` után a `jsonPath()` metódust hívjuk meg, ennek paraméterül át kell adni a 
JSONPath kifejezést. A WebClient ezt lefuttatja, és erre megint adhatunk meg különböző asserteket.

Abban az esetben, ha ezt a JSON-t objektummá szeretnénk alakítani (deszerializálni), akkor az alábbi 
kódrészlet lesz segítségünkre:

```java
.expectBody(EmployeeDto.class).value(e -> assertEquals("John Doe", e.getName()));
```

Itt az expectBody()-nak paraméterül át kell adni, hogy milyen osztály példányává szeretnénk átalakítani, 
majd utána a `value()` metódus meghívásakor rendelkezésünkre áll már az objektum, és erre tudunk asserteket írni.

Ha azonban a válasz egy lista, akkor azt az `expectBodyList()` metódus segítségével alakíthatjuk a 
megfelelő típusú objektumok listájává, amelyre ezután különböző asserteket adhatunk meg (például a 
lista méretére vagy tartalmára vonatkozóan).

```java
.expectBodyList(EmployeeDto.class).hasSize(2).contains(new EmployeeDto(1L, "John Doe"));
```