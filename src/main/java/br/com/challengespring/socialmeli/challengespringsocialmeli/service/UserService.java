package br.com.challengespring.socialmeli.challengespringsocialmeli.service;

import br.com.challengespring.socialmeli.challengespringsocialmeli.dto.FollowedSellerDTO;
import br.com.challengespring.socialmeli.challengespringsocialmeli.dto.SellerDTO;
import br.com.challengespring.socialmeli.challengespringsocialmeli.dto.UserDTO;
import br.com.challengespring.socialmeli.challengespringsocialmeli.entity.Post;
import br.com.challengespring.socialmeli.challengespringsocialmeli.entity.User;
import br.com.challengespring.socialmeli.challengespringsocialmeli.entity.Seller;
import br.com.challengespring.socialmeli.challengespringsocialmeli.exceptions.*;
import br.com.challengespring.socialmeli.challengespringsocialmeli.repository.UserRepository;
import br.com.challengespring.socialmeli.challengespringsocialmeli.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    UserRepository repository;

    @Autowired
    SellerRepository vendorRepository;

    public UserDTO newUser(User user){
        validUser(user);
        try {
            repository.save(user);
            return new UserDTO(user.getIdUser(), user.getUserName());
        }catch (ErroCriarUsuarioException error){
            throw new ErroCriarUsuarioException("Erro ao criar um novo usuario.");
        }
    }

    public UserDTO getUserById(Long idUser) {
        validIdNotNull(idUser);
        Optional<User> user = repository.findById(idUser);
        if(!user.isPresent()){
            throw new RegistroNaoEncontradoException("Registro de usuario nao existe ou nao foi encontrado.");
        }
        return new UserDTO(user.get().getIdUser(), user.get().getUserName());
    }

    //TODO: Adicionar Exceptions caso de erro na execucao de seguir um usuario
    //TODO: Caso ja esteja seguindo o usuario, nao permitir seguir novamente e lancar uma exception
    public FollowedSellerDTO follow(Long idUser, Long userIdToFollow){
        validUserIsPresent(idUser);
        validUserIsPresent(userIdToFollow);
        Optional<User> user = repository.findById(idUser);
        Optional<Seller> seller = vendorRepository.findById(userIdToFollow);

        List<User> listUser = seller.get().getFollowers();

        Boolean verify = listUser.contains(user.get());
        System.out.println("contain" + verify );

        if (idUser!=userIdToFollow || verify!=true){
            seller.get().getFollowers().add(user.get());
            Long i = Long.valueOf(seller.get().getFollowersCount().intValue() + 1);
            seller.get().setFollowersCount(i);
            user.get().getFollowed().add(seller.get());
            repository.save(user.get());
        } else {
            throw new IdNaoPodeSerRepetidoException("O id nao pode ser repetido.");
        }
        return new FollowedSellerDTO(user.get().getIdUser(), user.get().getUserName(),
                new SellerDTO(seller.get().getIdUser(), seller.get().getUserName(),
                        seller.get().getIsSeller(), seller.get().getFollowersCount()));
    }

    //  US 0007 - Ser capaz de realizar a ação de “Deixar de seguir” (parar de seguir) um determinado vendedor.
    //TODO: Adicionar Exceptions caso de erro na execucao de deixar de seguir um usuario
    public FollowedSellerDTO unfollow(Long idUser, Long userIdToUnfollow){
        validUserIsPresent(idUser);
        validUserIsPresent(userIdToUnfollow);
        Optional<User> user = repository.findById(idUser);
        Optional<Seller> seller = vendorRepository.findById(userIdToUnfollow);
        Long i = Long.valueOf(seller.get().getFollowersCount().intValue() - 1);
        seller.get().setFollowersCount(i);
        seller.get().getFollowers().remove(user.get());
        user.get().getFollowed().remove(seller.get());
        repository.save(user.get());
        vendorRepository.save(seller.get());
        return new FollowedSellerDTO(user.get().getIdUser(), user.get().getUserName(),
                new SellerDTO(seller.get().getIdUser(), seller.get().getUserName(),
                        seller.get().getIsSeller(), seller.get().getFollowersCount()));
    }

    //TODO: Definir como retornar quando a lista de usuarios seguindo estiver vazia
    public List<Seller> listFollowers (Long idUser){
        validUserIsPresent(idUser);
        Optional<User> userIn = repository.findById(idUser);
        if (userIn.get().getFollowed().isEmpty()){
            throw new ErroListarUsuariosExceptions("Nao existem usuarios para listar");
        }
        return userIn.get().getFollowed();
    }

    //TODO: Definir como retornar quando a lista de usuarios seguindo estiver vazia
    public List<Post> getPostListOfSellersThatUserFollows (Long idUser){
        validUserIsPresent(idUser);
        Optional<User> user = repository.findById(idUser);

        int size = user.get().getFollowed().size();

        if (user.get().getFollowed().isEmpty()){
            throw new ErroListarUsuariosExceptions("Nao existem usuarios para listar");
        }

        List<Post> listPost = new ArrayList<>();
        for (int i = 0; i >= size; i++ ){
            listPost = user.get().getFollowed().get(i).getPost();
        }
        return listPost;
    }

    private void validUserIsPresent(Long idUser) {
        validIdNotNull(idUser);
        boolean user = repository.existsById(idUser);
        if (false == user){
            throw new RegistroNaoEncontradoException("Registro de usuario nao existe ou nao foi encontrado.");
        }
    }

    private Boolean validIdNotNull(Long idUser){
        if (Objects.isNull(idUser)){
            throw new IdNaoPodeSerNuloException("Campo Id não pode estar vazio");
        }
        return true;
    }

    private User validUser(User user) {
        if (user!=null){
            return user;
        }
        throw new ValorNaoPodeSerNuloException("Usuario nao pode ser nulo.");
    }

    // Get
    // localhost:4200/users/{userId}/followers/list?order=name_asc
    // /users/{UserID}/followers/list?order=name_desc
    // /users/{UserID}/followed/list?order=name_asc
    // /users/{UserID}/followed/list?order=name_desc
    // US 0008: Ordem alfabética crescente e decrescente

}
