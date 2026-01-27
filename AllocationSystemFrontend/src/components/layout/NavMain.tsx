import { ChevronRight, type LucideIcon } from "lucide-react"
import { Link, useLocation } from "react-router-dom"
import { useState } from "react"

import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible"
import {
  SidebarGroup,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuSubItem,
} from "@/components/ui/sidebar"

export type NavItem = {
  title: string
  url: string
  icon: LucideIcon
  isActive?: boolean
  items?: {
    title: string
    url: string
  }[]
}

export type NavGroup = {
  label: string
  items: NavItem[]
}

const isSubItemActive = (location: { pathname: string }, subItemUrl: string) =>
  location.pathname === subItemUrl

const isItemActive = (location: { pathname: string }, item: NavItem) => {
  if (item.isActive !== undefined) return item.isActive
  if (item.items?.length) {
    return item.items.some((subItem) => location.pathname === subItem.url)
  }
  return location.pathname === item.url
}

function NavMainItem({
  item,
  openItems,
  setOpenItems
}: {
  item: NavItem;
  openItems: Set<string>;
  setOpenItems: React.Dispatch<React.SetStateAction<Set<string>>>
}) {
  const location = useLocation()
  const hasSubItems = item.items && item.items.length > 0
  const itemActive = isItemActive(location, item)
  const isOpen = hasSubItems ? openItems.has(item.title) : false

  const handleOpenChange = (open: boolean) => {
    if (hasSubItems) {
      setOpenItems((prev) => {
        const next = new Set(prev)
        if (open) {
          next.add(item.title)
        } else {
          next.delete(item.title)
        }
        return next
      })
    }
  }

  return (
    <Collapsible
      asChild
      open={isOpen}
      onOpenChange={handleOpenChange}
    >
      <SidebarMenuItem>
        {hasSubItems ? (
          <CollapsibleTrigger asChild>
            <SidebarMenuButton
              tooltip={item.title}
              isActive={false}
              className="group"
            >
              <item.icon />
              <span>{item.title}</span>
              <ChevronRight className="ml-auto transition-transform duration-200 group-data-[state=open]:rotate-90" />
            </SidebarMenuButton>
          </CollapsibleTrigger>
        ) : (
          <SidebarMenuButton
            asChild
            tooltip={item.title}
            isActive={itemActive}
          >
            <Link to={item.url}>
              <item.icon />
              <span>{item.title}</span>
            </Link>
          </SidebarMenuButton>
        )}
        {hasSubItems && (
          <CollapsibleContent>
            <SidebarMenuSub>
              {item.items?.map((subItem) => (
                <SidebarMenuSubItem key={subItem.title}>
                  <SidebarMenuSubButton
                    asChild
                    isActive={isSubItemActive(location, subItem.url)}
                  >
                    <Link to={subItem.url}>
                      <span>{subItem.title}</span>
                    </Link>
                  </SidebarMenuSubButton>
                </SidebarMenuSubItem>
              ))}
            </SidebarMenuSub>
          </CollapsibleContent>
        )}
      </SidebarMenuItem>
    </Collapsible>
  )
}

export function NavMain({
  groups,
}: {
  groups: NavGroup[]
}) {
  const location = useLocation()

  const [openItems, setOpenItems] = useState<Set<string>>(() => {
    const initialOpen = new Set<string>()
    groups.forEach((group) => group.items.forEach((item) => {
      if (item.items?.length) {
        const hasActiveChild = item.items.some((subItem) => location.pathname === subItem.url)
        const isBaseData = item.url.startsWith("/base-data")
        if (hasActiveChild || isBaseData) {
          initialOpen.add(item.title)
        }
      }
    }))
    return initialOpen
  })

  return (
    <>
      {groups.map((group) => (
        <SidebarGroup key={group.label}>
          <SidebarGroupLabel>{group.label}</SidebarGroupLabel>
          <SidebarMenu>
            {group.items.map((item) => (
              <NavMainItem
                key={item.title}
                item={item}
                openItems={openItems}
                setOpenItems={setOpenItems}
              />
            ))}
          </SidebarMenu>
        </SidebarGroup>
      ))}
    </>
  )
}

