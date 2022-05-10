package sptech.projetojpa06.controle;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sptech.projetojpa06.entidade.AnimalEstimacao;
import sptech.projetojpa06.repositorio.AnimalEstimacaoRepository;
import sptech.projetojpa06.resposta.ConsultaAnimalResposta;
import sptech.projetojpa06.restviacep.CepResposta;
import sptech.projetojpa06.restviacep.ClientViaCep;

import javax.persistence.Column;
import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping("/pets")
public class AnimalEstimacaoController {

    @Autowired
    private AnimalEstimacaoRepository repository;

    private byte[] foto;

    @Autowired
    private ClientViaCep clientViaCep;

    @PatchMapping(value = "/foto/{codigo}", consumes = "image/jpeg")
    public ResponseEntity<Object> patchFoto(@PathVariable long codigo, @RequestBody byte[] novaFoto) {

        if (!repository.existsById(codigo)) {
            return ResponseEntity.status(404).build();
        }

        AnimalEstimacao petEncontrado = repository.findById(codigo).get();
        petEncontrado.setFoto(novaFoto);
        repository.save(petEncontrado);

        return ResponseEntity.status(200).build();
    }

    @GetMapping(value = "/foto/{codigo}", produces = "image/jpeg")
    public ResponseEntity getFoto(@PathVariable long codigo) {

        if (!repository.existsById(codigo)) {
            return ResponseEntity.status(404).build();
        }

        AnimalEstimacao petEncontrado = repository.findById(codigo).get();
        return ResponseEntity.status(200).body(petEncontrado.getFoto());
    }

    @PatchMapping("/endereco/{codigo}/{cep}/{complemento}")
    public ResponseEntity<Object> patchEndereco(
        @PathVariable long codigo,
        @PathVariable String cep,
        @PathVariable String complemento
    ) {
        CepResposta cepEncontrado = clientViaCep.getCep(cep);
        AnimalEstimacao petEncontrado = repository.findById(codigo).get();

        petEncontrado.setBairro(cepEncontrado.getBairro());
        petEncontrado.setMunicipio(cepEncontrado.getLocalidade());
        petEncontrado.setEndereco(cepEncontrado.getLogradouro() + cepEncontrado.getComplemento());
        petEncontrado.setUf(cepEncontrado.getUf());

        repository.save(petEncontrado);

        return ResponseEntity.status(200).build();
    }

    @PostMapping
    public ResponseEntity postPet(
            @RequestBody @Valid AnimalEstimacao novoPet) {
        repository.save(novoPet);
        return status(201).build();
    }

    @GetMapping
    public ResponseEntity<List<AnimalEstimacao>> getPets() {
        return status(200).body(repository.findAll());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200",
                     content = @Content(mediaType = "text/csv"))
    })
    @GetMapping("/relatorio")
    public ResponseEntity getRelatorio() {
        List<AnimalEstimacao> lista = repository.findAll();
        String relatorio = "";
        for (AnimalEstimacao pet : lista) {
            relatorio += pet.getCodigo()+","+pet.getNome()+"\n";
        }
        return status(200)
                    .header("content-type", "text/csv")
                    .header("content-disposition", "filename=\"relatorio-de-pets.csv\"")
                    .body(relatorio);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<AnimalEstimacao> getPet(@PathVariable long codigo) {
        return of(repository.findById(codigo));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> deletePet(@PathVariable long codigo) {
        repository.deleteById(codigo);
        return status(200).build();
    }

    @GetMapping("/filtro/{nome}")
    public ResponseEntity<List<AnimalEstimacao>> filtro(
            @PathVariable String nome
    ) {
        return status(200).body(repository.findByNome(nome));
    }

    @GetMapping("/filtro/{nome}/{castrado}")
    public ResponseEntity<List<AnimalEstimacao>> filtro(
            @PathVariable String nome,
            @PathVariable boolean castrado
    ) {
        return status(200)
                .body(repository.findByNomeAndCastrado(nome, castrado));
    }

    /*
    GET /pets/tipo/{codigoTipo}
    Ex: /pets/tipo/1 -> traria todos os pets que são "gato"
     */
    @GetMapping("/tipo/{codigoTipo}")
    public ResponseEntity getPorTipo(@PathVariable int codigoTipo) {
        List<AnimalEstimacao> lista = repository.findByTipoCodigo(codigoTipo);
        if (lista.isEmpty()) {
            return status(204).build();
        } else {
            return status(200).body(lista);
        }
    }

    /*
    GET /pets/tipo-nome/{nomeTipo}
    Ex: /pets/tipo-nome/cho -> traria todos os pets contém "cho" na descricao do tipo
     */
    @GetMapping("/tipo-nome/{nomeTipo}")
    public ResponseEntity getPorDescricao(@PathVariable String nomeTipo) {
        List<AnimalEstimacao> lista = repository.findByTipoDescricaoContains(nomeTipo);
        if (lista.isEmpty()) {
            return status(204).build();
        } else {
            return status(200).body(lista);
        }
    }

    @GetMapping("/filtro-simples/{nome}")
    public ResponseEntity getSimplesPorNome(@PathVariable String nome) {
        List<ConsultaAnimalResposta> lista =
                repository.consultaSimplePorNome("%"+nome+"%");
        if (lista.isEmpty()) {
            return status(204).build();
        } else {
            return status(200).body(lista);
        }
    }
}
