package com.xxxx.seckill.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther:huhao
 * @Date:2021/8/23-14:21
 * @Description:com.xxxx.seckill.pojo
 * @version:1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage {
    private User user;
    private Long goodId;
}
