import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.qiu.qoj.document.DocumentApplication;
import com.qiu.qoj.document.model.dto.card.CardUpdateRequest;
import com.qiu.qoj.document.model.entity.Card;
import com.qiu.qoj.document.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest(classes = DocumentApplication.class)
public class testMongo {

    @Autowired
    private GroupRepository groupRepository;


@Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void test(){
        System.out.println(mongoTemplate.getDb().getName());
    }

    @Test
    public void test1(){
        System.out.println(groupRepository.findByUserId(1l));
    }

    @Test
    public void test2(){
        Card card = new Card();
        card.setQuestion("123");

        CardUpdateRequest cardUpdateRequest = new CardUpdateRequest();
        cardUpdateRequest.setQuestion("454353");
        BeanUtil.copyProperties(cardUpdateRequest,card, CopyOptions.create().setIgnoreNullValue(true).setIgnoreProperties(Card::getQuestion));
        System.out.println(card);
    }
}
