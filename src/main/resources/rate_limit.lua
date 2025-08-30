-- 基于ZSet滑动窗口的限流Lua脚本
-- 参数说明：
-- KEYS[1]: 限流key (rate_limit:userId:ticketId 或 rate_limit:ticketId)
-- ARGV[1]: 当前时间戳(毫秒)
-- ARGV[2]: 窗口大小(毫秒)
-- ARGV[3]: 最大请求数
-- ARGV[4]: 用户ID(可选，用于用户级别限流)

local key = KEYS[1]
local currentTime = tonumber(ARGV[1])
local windowSize = tonumber(ARGV[2])
local maxRequests = tonumber(ARGV[3])
local userId = ARGV[4] or ""

-- 计算窗口开始时间
local windowStart = currentTime - windowSize

-- 移除窗口外的数据
redis.call('zremrangebyscore', key, 0, windowStart)

-- 获取当前窗口内的请求数量
local currentCount = redis.call('zcard', key)

-- 检查是否超过限流阈值
if currentCount >= maxRequests then
    return 0  -- 限流，拒绝请求
end

-- 添加当前请求到ZSet
local score = currentTime
local member = userId .. ":" .. currentTime
redis.call('zadd', key, score, member)

-- 设置过期时间(窗口大小 + 1秒，防止内存泄漏)
redis.call('expire', key, math.ceil((windowSize + 1000) / 1000))

-- 返回当前窗口内的请求数量
return currentCount + 1
