package sptech.projetojpa06.restviacep;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "viacep", url = "https://viacep.com.br/ws/")
public interface ClientViaCep {
    
    @GetMapping("{cep}/json")
    public CepResposta getCep(@PathVariable String cep);
    
}
