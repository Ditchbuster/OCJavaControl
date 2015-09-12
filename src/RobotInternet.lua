local component = require("component")
local internet = require("internet")
local term = require("term")
local text = require("text")
local event = require("event")
local shell = require("shell")
local robot = require("robot")
local nav = require("navigation")

if not component.isAvailable("internet") then
  io.stderr:write("requires an Internet Card to run!\n")
  return
end
if not component.isAvailable("robot") then
    io.stderr:write("requires a robot to run!\n")
    return
end

--********************* List functions *************
List = {}
function List.new()
    return {first = 0, last = -1}
end

function List.pushleft (list, value)
    local first = list.first - 1
    list.first = first
    list[first] = value
end

function List.pushright (list, value)
    local last = list.last + 1
    list.last = last
    list[last] = value
end

function List.popleft (list)
    local first = list.first
    if first > list.last then error("list is empty") end
    local value = list[first]
    list[first] = nil        -- to allow garbage collection
    list.first = first + 1
    return value
end

function List.popright (list)
    local last = list.last
    if list.first > last then error("list is empty") end
    local value = list[last]
    list[last] = nil         -- to allow garbage collection
    list.last = last - 1
    return value
end

function List.isEmpty (list)
    if list.first > list.last then
        return true
    else
        return false
    end
end
--***************** End List functions ********************


local cmdList = List.new()

local args, options = shell.parse(...)
if #args < 1 then
  print("Usage: telnet <server:port>")
  return
end

local host = args[1]

local gpu = component.gpu
local w, h = gpu.getResolution()

local hist = {}

local sock, reason = internet.open(host)
if not sock then
  io.stderr:write(reason .. "\n")
  return
end

sock:setTimeout(0.05)

sock:write("robot: " .. robot.name() .. "\r\n")

--Function from the built in IRC program
local function print(message, overwrite)
  local w, h = component.gpu.getResolution()
  local line
  repeat
    line, message = text.wrap(text.trim(message), w, w)
    if not overwrite then
      component.gpu.copy(1, 1, w, h - 1, 0, -1)
    end
    overwrite = false
    component.gpu.fill(1, h - 1, w, 1, " ")
    component.gpu.set(1, h - 1, line)
  until not message or message == ""
end

local function validate(line) --add in strings that are commands
    local cmd = string.sub(line,1,3)
    if(cmd == "FWD") then
        return true
    end
    return false
end

local function cmd(line)
    --print("Server:")
    local cmd = string.sub(line,1,3)
    --local cmd=line
    print(cmd)
    if(cmd=="FWD") then
        local rtn = true
        local moves = string.match(line,"%d+")
        local x
        for x = 0, moves do
            rtn=robot.forward()
            if(not rtn) then
                return rtn, x
            end

        end
        return rtn, x
    elseif (cmd=="STS") then
        sock:write("STS " .. string.tostring(nav.getPosition()) )
    end
        --print("end cmd")
end

local function draw()
    if not sock then
    return false
  end
  repeat
      local ok, line = pcall(sock.read, sock)
      if ok then
          --print("ok")
          print(line)
          if not line then
              print("Connection lost.")
              sock:close()
              sock = nil
              return false
          end
          if(validate(line)) then
              List.pushright(cmdList,text.trim(line)) --trim whitespace off end and start and insert into table
          end
      end
  until not ok

end

local function uin()
  term.setCursor(1,h)
  line = term.read(hist)
  line2 = text.trim(line)
  if line2 == "\\exit" then
    return false
  elseif line2 == "run" then
      while(not List.isEmpty(cmdList)) do
          print("running...")
          cmd(List.popleft(cmdList))
      end
  else
    sock:write(line2.."\r\n")
  end
  return true
end

local going = true
local dLoop = event.timer(0.5, draw, math.huge)

repeat
  r = uin()

until not r

if dLoop then
  event.cancel(dLoop)
end

if sock then
  sock:write("logout\r\n")
  sock:close()
end


