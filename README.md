# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD A20 - Alameda  

Pedro Guerreiro, 78264, afonso.guerreiro.pedro@gmail.com

João Esteves, 78304, joao_p_esteves@hotmail.com

Manuel Santos, 78445, manuel.jss@hotmail.com

Repositório:
[tecnico-distsys/A_20-project](https://github.com/tecnico-distsys/A_20-project/)

-------------------------------------------------------------------------------

## Instruções de instalação 


### Ambiente

[0] Iniciar sistema operativo

Linux (OpenSuse Leap 42.1 - PC's RNL) 

[1] Iniciar servidores de apoio

```
JUDDI: executar o comando ./startup.sh na diretoria <path_to_tomcat>/bin 
```

[2] Criar pasta temporária

```
mkdir A20_SD
cd A20_SD
```


[3] Obter código fonte do projeto (versão entregue)

```
git clone https://github.com/tecnico-distsys/A_20-project
cd A_20-project
git checkout tags/FinalRelease
```


[4] Instalar módulos de bibliotecas auxiliares

Fazer Download da Biblioteca UddiNaming em [UDDINaming](http://disciplinas.tecnico.ulisboa.pt/leic-sod/2015-2016/labs/05-ws1/uddi-naming.zip)
e instalar a dependencia no maven.
```
cd uddi-naming
mvn clean install
```

-------------------------------------------------------------------------------
### Serviço CERTIFICATE AUTHORITY

[1] Construir e executar **servidor**

```
cd ca-ws
mvn clean install
mvn exec:java
```

[2] Construir **cliente** e executar testes unitários

```
cd ca-ws-cli
mvn clean install
```

-------------------------------------------------------------------------------
### Serviço WS HANDLERS

[1] Construir **handlers**

```
cd ws-handlers
mvn clean install
```

-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor**


```
cd transporter-ws
mvn clean install exec:java -Dws.i=x
mvn exec:java
```


[2] Construir **cliente** e executar testes de integração

```
cd transporter-ws-cli
mvn clean install
```

-------------------------------------------------------------------------------

### Serviço BROKER

[1] Construir e executar **servidor primário**

```
cd broker-ws
mvn clean install
mvn exec:java
```

[2] Construir e executar **servidor secundário**

```
cd broker-ws
mvn clean install
mvn exec:java
```


[3] Construir **cliente** e executar testes

```
cd broker-ws-cli
mvn clean install exec:java
```


-------------------------------------------------------------------------------
**FIM**
